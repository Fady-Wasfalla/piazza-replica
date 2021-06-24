package RabbitMQ;

import ServiceNettyServer.ServiceNettyHTTPServer;
import Services.PostgreSQL;
import Services.jedis;
import Services.mongoDB;
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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ConsumerMQ {
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void run(String microservice, int port) throws Exception {
        Dotenv dotenv = Dotenv.load();
        CommandsMap cmdMap = new CommandsMap();
        CommandsMap.instantiate();
        System.out.println("Command Map Size: " + CommandsMap.cmdMap.size());
        CommandsMap.getAllClasses();
        Consumer consumer;
        if (MessageQueue.channel == null)
            MessageQueue.instantiateChannel();

        jedis jedis = null;
        try {
            mongoDB.initMongo();
            jedis = new jedis(dotenv.get("redis_host", "localhost"), 6379, "");
            PostgreSQL.initPostgres(-1);
        } catch (Exception error) {
            System.out.println("ERROR CREATING MONGODB/REDIS/POSTGRES CONNECTION :" + error);

        }
        jedis finalJedis = jedis;

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
                        String message = new String(body, StandardCharsets.UTF_8);
                        JSONObject requestJson = new JSONObject(message);
                        String function = requestJson.getString("function");
                        String queue = requestJson.getString("queue");

                        System.out.println("Method to be found: " + queue + "/" + function);
                        CommandDP command = (CommandDP) CommandsMap.queryClass(queue + "/" + function).getDeclaredConstructor().newInstance();
                        Class service = command.getClass();
                        Method setData = service.getMethod("setData", JSONObject.class, jedis.class);
                        setData.invoke(command, requestJson, finalJedis);
                        JSONObject result = command.execute();
                        MessageQueue.send(result.toString(), properties.getReplyTo(), properties.getCorrelationId());
                        MessageQueue.channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JSONObject result = new JSONObject();
                        System.out.println(e);
                        result.put("Err Message MQ", e.toString());
                        System.out.println(result);
                        try {
                            System.out.println("ConsumerMQ send message inside try: " + result);
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
        System.out.println("Request queue name (consumer): " + REQ_QUEUE_NAME);
        MessageQueue.channel.basicConsume(REQ_QUEUE_NAME, false, consumer);

        System.out.println("User Service Server is UP");
        ServiceNettyHTTPServer serviceServer = new ServiceNettyHTTPServer(port, microservice, cmdMap);
        serviceServer.start();
    }
}
