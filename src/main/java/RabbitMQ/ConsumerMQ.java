package RabbitMQ;

import com.rabbitmq.client.*;
import core.CommandDP;
import core.CommandsMap;
import core.commands.UserCommands.UserDAL;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerMQ {
    //private final static String QUEUE_NAME = "queue_name";
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);
    static String[] queueNames = {
            "chatRequestQueue" ,"chatResponseQueue" ,
            "courseRequestQueue" ,"courseResponseQueue" ,
            "mediaRequestQueue" ,"mediaResponseQueue" ,
            "notificationRequestQueue" ,"notificationResponseQueue" ,
            "pollRequestQueue" ,"pollResponseQueue" ,
            "questionRequestQueue" ,"questionResponseQueue" ,
            "userRequestQueue" ,"userResponseQueue", "queue_name"
    };

    public static void main(String[] argv) throws Exception {
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

        for (String QUEUE_NAME:queueNames) {

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

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

                            // Several Instances of DAL
//                    Class dalClass = Class.forName("core.commands."+serviceDAL+"Commands."+serviceDAL+"DAL");
//                    Object dal = dalClass.newInstance();

                            Method setData = service.getMethod("setData",JSONObject.class, Object.class);

                            setData.invoke(command, requestJson, dalMap.get("core.commands."+serviceDAL+"Commands."+serviceDAL+"DAL"));
                            command.execute();
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
