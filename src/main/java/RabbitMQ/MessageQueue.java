package RabbitMQ;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MessageQueue {
    public static ConnectionFactory factory;
    public static Connection connection;
    public static com.rabbitmq.client.Channel channel;

    public static void instantiateChannel() {
        System.out.println("Instantiate Channel MessageQueue");
        try {
            factory = new ConnectionFactory();
            factory.setHost("localhost");
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

//    public static void send(String message, String queue, String corrId) throws IOException, TimeoutException {
//        Producer P = new Producer(queue);
//        P.send(message, corrId);
//    }

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
        MessageQueue.channel.basicPublish("", queue, props, message.getBytes("UTF-8"));
        //TODO clean resources When closing the project (channel and connection)
//        MessageQueue.channel.close();
//        MessageQueue.connection.close();
    }
}
