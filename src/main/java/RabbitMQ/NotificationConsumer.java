package RabbitMQ;

public class NotificationConsumer {
    public static void main(String[] args) throws Exception {
        ConsumerMQ.run("notification", 8084);
    }
}
