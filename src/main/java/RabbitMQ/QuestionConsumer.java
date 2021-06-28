package RabbitMQ;

public class QuestionConsumer {
    public static void main(String[] args) throws Exception {
        ConsumerMQ.run("question", 8082);
    }
}
