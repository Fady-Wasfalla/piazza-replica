package RabbitMQ;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import ServiceNettyServer.ServiceNettyHTTPServer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.rabbitmq.client.*;
import core.CommandDP;
import core.CommandsMap;
import core.commands.UserCommands.UserDAL;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import io.github.cdimascio.dotenv.Dotenv;


public class ConsumerMQ {
    //private final static String QUEUE_NAME = "queue_name";
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);


    public static void main(String[] args) throws Exception {
        String microservice = "";
        if(args.length>0){
            microservice = args[0];
        }else{
            throw new Exception("Enter Microservice Name");
        }

        Dotenv dotenv = Dotenv.load();
        String strlist = dotenv.get("queuesReq");
        List<String> queueReqNames = Arrays.asList(strlist.split(","));
        strlist = dotenv.get("queuesRes");
        List<String> queueResNames = Arrays.asList(strlist.split(","));

        if(!queueReqNames.contains(microservice+"Req")){
            throw new Exception("Microservice Does Not Exist");
        }
        if(!queueResNames.contains(microservice+"Res")){
            throw new Exception("Microservice Does Not Exist");
        }

        CommandsMap cmdMap = new CommandsMap();
        cmdMap.instantiate();
        System.out.println(cmdMap.cmdMap.size());
        System.out.println("yes");
        cmdMap.getAllClasses();

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
        try  {
            mongoClient = MongoClients.create(connectionString);
        }catch(Exception error){
            System.out.println("error no. of DP connections :"+error);
        }
        MongoClient finalMongoClient = mongoClient;

        NettyHTTPServer.channel.queueDeclare(microservice+"Res", false, false, false, null);

        String QUEUE_NAME = microservice+"Req";

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
                            cmdMap.getAllClasses();
                            CommandDP command = (CommandDP) cmdMap.queryClass(queue + "/" + function).getDeclaredConstructor().newInstance();
                            Class service = command.getClass();
                            Method setData = service.getMethod("setData",JSONObject.class, MongoClient.class);
                            setData.invoke(command, requestJson, finalMongoClient);
                            JSONObject result = command.execute();
                            System.out.println("MQ ");
                            NettyServerHandler.sendMessageToActiveMQ(result.toString(), properties.getReplyTo(), properties.getCorrelationId());
//                                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
//                                        .Builder()
//                                        .correlationId(properties.getCorrelationId())
//                                        .build();
//                                channel.basicPublish("", properties.getReplyTo(), replyProps, result.toString().getBytes("UTF-8"));
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        } catch (Exception e) {
                            e.printStackTrace();
//                                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
//                                        .Builder()
//                                        .correlationId(properties.getCorrelationId())
//                                        .build();
                            JSONObject result = new JSONObject();
                            result.put("Err Message MQ",e.toString());
                            System.out.println(result.toString());
                            try {
//                                    channel.basicPublish("", properties.getReplyTo(), replyProps, result.toString().getBytes("UTF-8"));
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

        System.out.println("User Service Server is UP");
        ServiceNettyHTTPServer serviceServer = new ServiceNettyHTTPServer(8081,microservice,cmdMap);
        serviceServer.start();

    }

}
