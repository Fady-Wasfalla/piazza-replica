package RabbitMQ;

public class UserConsumer {
    public static void main(String[] args) throws Exception {
        ConsumerMQ.run("user",8081);
    }
}
