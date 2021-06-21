package RabbitMQ;

public class CourseConsumer {
    public static void main(String[] args) throws Exception {
        ConsumerMQ.run("course",8086);
    }
}
