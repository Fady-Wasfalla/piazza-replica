package RabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    public void send(String message) throws IOException, TimeoutException {
        this.channel.basicPublish("", this.queue, null, message.getBytes("UTF-8"));
        this.channel.close();
        this.connection.close();
    }


}

