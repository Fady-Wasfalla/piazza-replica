package RabbitMQ;

import NettyHTTP.NettyHTTPServer;
import com.rabbitmq.client.*;
import core.CommandDP;
import core.CommandsMap;
import core.commands.UserCommands.UserDAL;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.github.cdimascio.dotenv.Dotenv;


public class ConsumerMQ {
    //private final static String QUEUE_NAME = "queue_name";
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] argv) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String strlist = dotenv.get("queuesReq");
        List<String> queueReqNames = Arrays.asList(strlist.split(","));
        strlist = dotenv.get("queuesRes");
        List<String> queueResNames = Arrays.asList(strlist.split(","));
        CommandsMap.instantiate();
        // One Instance of DAL
        ConcurrentMap<String, Object> dalMap = new ConcurrentHashMap<>();
        dalMap.put("core.commands.UserCommands.UserDAL", UserDAL.class.newInstance());
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel;
        Consumer consumer;
        channel = connection.createChannel();

        if(NettyHTTPServer.channel==null)
            NettyHTTPServer.instantiateChannel();

        for (String QUEUE_NAME:queueResNames) {
            NettyHTTPServer.channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        }

        for (String QUEUE_NAME:queueReqNames) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String responseQueue = QUEUE_NAME.split("Req",0)[0]+"Res";
            System.out.println("[*REQ] "+QUEUE_NAME + " [*] Waiting for messages. To exit press CTRL+C");


            consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    Runnable task = new Runnable() {
                        public void run() {
                            try {
                                String message = new String(body, "UTF-8");
                                JSONObject requestJson = new JSONObject(message);
                                String function = requestJson.getString("function");
                                String serviceDAL = requestJson.getString("service");
                                CommandDP command = (CommandDP) CommandsMap.queryClass(function).newInstance();
                                Class service = command.getClass();
                                Method setData = service.getMethod("setData",JSONObject.class, Object.class);
                                setData.invoke(command, requestJson, dalMap.get("core.commands."+serviceDAL+"Commands."+serviceDAL+"DAL"));
                                JSONObject result = command.execute();
                                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                                        .Builder()
                                        .correlationId(properties.getCorrelationId())
                                        .build();
                                channel.basicPublish("", properties.getReplyTo(), replyProps, result.toString().getBytes("UTF-8"));
                                channel.basicAck(envelope.getDeliveryTag(), false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    threadPool.submit(task);
                }
            };
            channel.basicConsume(QUEUE_NAME, false, consumer);
        }
    }
}
