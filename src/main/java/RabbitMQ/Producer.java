package RabbitMQ;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer {
    private String queue;
    private Channel channel;
    private Connection connection;

    public Producer(String queue) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(queue, false, false, false, null);

        this.channel = channel;
        this.queue = queue;
        this.connection = connection;
    }

    public void send(String message, String corrId) throws IOException, TimeoutException {
        String responseQueue = this.queue.split("Req",0)[0]+"Res";
        System.out.println(this.queue+" =====> "+message);
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(responseQueue)
                .build();
        this.channel.basicPublish("", this.queue, props, message.getBytes("UTF-8"));
        this.channel.close();
        this.connection.close();
    }


}

