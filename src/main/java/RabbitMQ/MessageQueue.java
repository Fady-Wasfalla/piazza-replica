package RabbitMQ;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.*;

public class MessageQueue {
    public static ConnectionFactory factory;
    public static Connection connection;
    public static com.rabbitmq.client.Channel channel;

    public static void instantiateChannel() {
        Dotenv dotenv = Dotenv.load();
        System.out.println("Instantiate Channel MessageQueue");
        if(channel!=null){
            System.out.println("Connection Already Instantiated");
        }
        for(int i=0;i<6;i++) {
            System.out.println("Attempting to connect to RabbitMQ trial: "+i);
            try {
                factory = new ConnectionFactory();
                factory.setHost(dotenv.get("rabbitmq_host", "localhost"));
                connection = factory.newConnection();
                channel = connection.createChannel();
                System.out.println("Connected to RabbitMQ successfully");
                String strlist = dotenv.get("queues");
                List<String> arr= Arrays.asList(strlist.split(","));
                for(String queue: arr){
                    declareQueue(queue);
                }
                return;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Error Connecting to RabbitMQ program exiting");
        System.exit(0);
    }

    public static void declareQueue(String microservice) throws IOException {
        //Response Queue Declare
        String RES_QUEUE_NAME = microservice + "Res";
        MessageQueue.channel.queueDeclare(RES_QUEUE_NAME, false, false, false, null);

        //Request Queue Declare
        String REQ_QUEUE_NAME = microservice + "Req";
        MessageQueue.channel.queueDeclare(REQ_QUEUE_NAME, false, false, false, null);

    }

    public static void send(String message, String queue, String corrId) throws IOException, TimeoutException {
        String responseQueue = queue.split("Req", 0)[0] + "Res";
        System.out.println("Send Message (MessageQueue.class):" + queue + " =====> " + message);
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(responseQueue)
                .build();
        System.out.println("Message===>" + message);
        //TODO to be removed
        if (MessageQueue.channel == null) {
               MessageQueue.instantiateChannel();
        }
        MessageQueue.channel.basicPublish("", queue, props, message.getBytes(StandardCharsets.UTF_8));
        //TODO clean resources When closing the project (channel and connection)
//        MessageQueue.channel.close();
//        MessageQueue.connection.close();
    }
}
