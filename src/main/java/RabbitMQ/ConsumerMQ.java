package RabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;

import core.commands.UserCommands.UserDAL;
import core.Command;
import core.CommandsMap;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConsumerMQ {
    private final static String QUEUE_NAME = "queue_name";

    public static void main(String[] argv) throws Exception {
        CommandsMap.instantiate();

        // One Instance of DAL
        ConcurrentMap<String, Object> dalMap = new ConcurrentHashMap<>();
        dalMap.put("core.commands.UserCommands.UserDAL", UserDAL.class.newInstance());


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
                String serviceDAL = requestJson.getString("service");

                try {
                    Class service = Class.forName("core.commands."+serviceDAL+"Commands."+function);
                    Command command = (Command) service.newInstance();

                    // Several Instances of DAL
//                    Class dalClass = Class.forName("core.commands."+serviceDAL+"Commands."+serviceDAL+"DAL");
//                    Object dal = dalClass.newInstance();

                    Method setData = service.getMethod("setData",JSONObject.class, Object.class);

                    setData.invoke(command, requestJson, dalMap.get("core.commands."+serviceDAL+"Commands."+serviceDAL+"DAL"));
                    command.execute();


                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}
