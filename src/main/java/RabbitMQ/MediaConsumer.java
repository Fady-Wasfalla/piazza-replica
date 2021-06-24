package RabbitMQ;

public class MediaConsumer {
    public static void main(String[] args) throws Exception {
        ConsumerMQ.run("media", 8085);
    }
}
