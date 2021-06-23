package RabbitMQ;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class MessageQueue {
    public static ConnectionFactory factory;
    public static Connection connection;
    public static com.rabbitmq.client.Channel channel;

    public static void instantiateChannel() {
        Dotenv dotenv = Dotenv.load();
        System.out.println("Instantiate Channel MessageQueue");
        try {
            factory = new ConnectionFactory();
            factory.setHost(dotenv.get("rabbitmq_host", "localhost"));
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
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
        if (MessageQueue.channel == null) {
            MessageQueue.instantiateChannel();
        }
        MessageQueue.channel.basicPublish("", queue, props, message.getBytes(StandardCharsets.UTF_8));
        //TODO clean resources When closing the project (channel and connection)
//        MessageQueue.channel.close();
//        MessageQueue.connection.close();
    }
}
