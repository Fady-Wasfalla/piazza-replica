package RabbitMQ;

import com.rabbitmq.client.*;
import core.Command;
import core.CommandsMap;
import core.commands.UserCommands.UserDAL;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.stream.LongStream;

public class ThreadPool {
    private static ExecutorService threadPool = Executors.newFixedThreadPool(3);
    private final static String QUEUE_NAME = "queue_name";


    public static void main(String[] args) throws Exception {

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

                Runnable task = new Runnable() {
                    public void run() {
                        long threadId = Thread.currentThread().getId();
                        System.out.println("Thread # " + threadId + " is doing this task");
                        LongStream.range(2,10000000000L).map(i -> i*i).reduce((i, j) -> i+j).getAsLong();
                        System.out.println("Done " + threadId);
                    }
                };
                threadPool.submit(task);
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}
