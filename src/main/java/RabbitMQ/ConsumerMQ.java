package RabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;

import core.Command;
import core.CommandsMap;
import core.commands.UserCommads.SignupCommand;
import org.json.JSONObject;

public class ConsumerMQ {
    private final static String QUEUE_NAME = "queue_name";

    public static void main(String[] argv) throws Exception {
        CommandsMap.instantiate();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                JSONObject requestJson = new JSONObject(message);
                String function = requestJson.getString("function");
                try {
                    Command o = (Command) CommandsMap.queryClass(function).newInstance();
                    o.data=requestJson;
                    o.execute();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}
