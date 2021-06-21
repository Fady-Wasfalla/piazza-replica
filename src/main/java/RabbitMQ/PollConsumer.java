package RabbitMQ;

public class PollConsumer {
    public static void main(String[] args) throws Exception {
        ConsumerMQ.run("poll",8083);
    }
}
