package RabbitMQ;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import Services.jedis;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.rabbitmq.client.*;
import core.CommandDP;
import core.CommandsMap;
import core.commands.UserCommands.UserDAL;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ConsumerMQ {
    //private final static String QUEUE_NAME = "queue_name";
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);


    public static void main(String[] argv) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String strlist = dotenv.get("queues");
        List<String> queueNames = Arrays.asList(strlist.split(","));
        CommandsMap.instantiate();
        // One Instance of DAL
        ConcurrentMap<String, Object> dalMap = new ConcurrentHashMap<>();
        dalMap.put("core.commands.UserCommands.UserDAL", UserDAL.class.getDeclaredConstructor().newInstance());
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel;
        Consumer consumer;
        channel = connection.createChannel();

        if(NettyHTTPServer.channel==null)
            NettyHTTPServer.instantiateChannel();
        String connectionString = dotenv.get("CONNECTION_STRING") + "10" ; // no. of DP connections
        MongoClient mongoClient = null;
        jedis jedis = null;
        try  {
            mongoClient = MongoClients.create(connectionString);
            jedis = new jedis("localhost", 6379, "");
        }catch(Exception error){
            System.out.println("ERROR CREATING MONGODB CONNECTION :"+error);
        }
        for (String QUEUE_NAME:queueNames) {
            QUEUE_NAME = QUEUE_NAME + "Res";
            NettyHTTPServer.channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        }
        MongoClient finalMongoClient = mongoClient;
        jedis finalJedis = jedis;
        for (String QUEUE_NAME:queueNames) {
            QUEUE_NAME = QUEUE_NAME +   "Req";
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
                                String queue = requestJson.getString("queue");
                                
                                CommandDP command = (CommandDP) CommandsMap.queryClass(queue + "/" + function).getDeclaredConstructor().newInstance();
                                Class service = command.getClass();
                                Method setData = service.getMethod("setData",JSONObject.class, MongoClient.class, jedis.class);
                                setData.invoke(command, requestJson, finalMongoClient, finalJedis);
                                JSONObject result = command.execute();
                                NettyServerHandler.sendMessageToActiveMQ(result.toString(), properties.getReplyTo(), properties.getCorrelationId());

                                channel.basicAck(envelope.getDeliveryTag(), false);
                            } catch (Exception e) {
                                e.printStackTrace();
                                JSONObject result = new JSONObject();
                                result.put("Message","Invalid d7kaaaaa here consumer ");
                                System.out.println(result.toString());
                                try {
                                    NettyServerHandler.sendMessageToActiveMQ(result.toString(), properties.getReplyTo(), properties.getCorrelationId());
                                    channel.basicAck(envelope.getDeliveryTag(), false);

                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
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
