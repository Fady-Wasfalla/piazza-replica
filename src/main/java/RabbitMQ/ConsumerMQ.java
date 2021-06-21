package RabbitMQ;

import ServiceNettyServer.ServiceNettyHTTPServer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import core.CommandDP;
import core.CommandsMap;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ConsumerMQ {
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void run(String microservice, int port) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String strlist = dotenv.get("queuesReq");
        List<String> queueReqNames = Arrays.asList(strlist.split(","));
        strlist = dotenv.get("queuesRes");
        List<String> queueResNames = Arrays.asList(strlist.split(","));

        if (!queueReqNames.contains(microservice + "Req")) {
            throw new Exception("Microservice Does Not Exist");
        }
        if (!queueResNames.contains(microservice + "Res")) {
            throw new Exception("Microservice Does Not Exist");
        }

        CommandsMap cmdMap = new CommandsMap();
        cmdMap.instantiate();
        System.out.println("Command Map Size: " + cmdMap.cmdMap.size());
        cmdMap.getAllClasses();
        Consumer consumer;
        if (MessageQueue.channel == null)
            MessageQueue.instantiateChannel();

        String connectionString = dotenv.get("CONNECTION_STRING") + "10"; // no. of DP connections
        MongoClient mongoClient = null;
        try {
            mongoClient = MongoClients.create(connectionString);
        } catch (Exception error) {
            System.out.println("error no. of DP connections :" + error);
        }
        MongoClient finalMongoClient = mongoClient;

        //Response Queue Declare
        String RES_QUEUE_NAME = microservice + "Res";
        MessageQueue.channel.queueDeclare(RES_QUEUE_NAME, false, false, false, null);

        //Request Queue Declare
        String REQ_QUEUE_NAME = microservice + "Req";
        MessageQueue.channel.queueDeclare(REQ_QUEUE_NAME, false, false, false, null);

        System.out.println("[*REQ] " + REQ_QUEUE_NAME + " [*] Waiting for messages. To exit press CTRL+C");

        consumer = new DefaultConsumer(MessageQueue.channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                Runnable task = () -> {
                    try {
                        System.out.println("Handle Delivery");
                        String message = new String(body, "UTF-8");
                        JSONObject requestJson = new JSONObject(message);
                        String function = requestJson.getString("function");
                        String queue = requestJson.getString("queue");
                        cmdMap.getAllClasses();
                        CommandDP command = (CommandDP) cmdMap.queryClass(queue + "/" + function).getDeclaredConstructor().newInstance();
                        Class service = command.getClass();
                        System.out.println("services ==> " + service);
                        Method setData = service.getMethod("setData", JSONObject.class, MongoClient.class);
                        setData.invoke(command, requestJson, finalMongoClient);
                        System.out.println("Before execution");
                        JSONObject result = command.execute();
                        System.out.println("After execution");
                        System.out.println("Consumer MQ invoke result:" + result.toString());
                        MessageQueue.send(result.toString(), properties.getReplyTo(), properties.getCorrelationId());
                        MessageQueue.channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JSONObject result = new JSONObject();
                        result.put("Err Message MQ", e.toString());
                        System.out.println(result.toString());
                        try {
                            System.out.println("ConsumerMQ send message inside try: " + result.toString());
//                                    channel.basicPublish("", properties.getReplyTo(), replyProps, result.toString().getBytes("UTF-8"));
                            MessageQueue.send(result.toString(), properties.getReplyTo(), properties.getCorrelationId());
                            MessageQueue.channel.basicAck(envelope.getDeliveryTag(), false);

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                };
                threadPool.submit(task);
            }
        };
        System.out.println("Request queue name (consumer): "+REQ_QUEUE_NAME);
        MessageQueue.channel.basicConsume(REQ_QUEUE_NAME, false, consumer);

        System.out.println("User Service Server is UP");
        ServiceNettyHTTPServer serviceServer = new ServiceNettyHTTPServer(port, microservice, cmdMap);
        serviceServer.start();
    }
}
