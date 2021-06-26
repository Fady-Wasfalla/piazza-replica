package core.commands.CourseCommands;

import RabbitMQ.MessageQueue;
import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DeleteCourseCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        String[] schema = {"courseId"};

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String courseId = data.getString("courseId");
        Document deletedCourse = mongoDB.deleteOne(Collections.course, new Document("_id", new ObjectId(courseId)), "_id");

        JSONObject pollDeleteRequest = new JSONObject();
        pollDeleteRequest.put("function", "DeleteCoursePollsCommand");
        pollDeleteRequest.put("queue", "poll");
        JSONObject pollBody = new JSONObject();
        pollBody.put("courseId", courseId);
        pollDeleteRequest.put("body", pollBody);

        JSONObject questionDeleteRequest = new JSONObject();
        questionDeleteRequest.put("function", "DeleteCourseQuestionsCommand");
        questionDeleteRequest.put("queue", "question");
        JSONObject questionBody = new JSONObject();
        questionBody.put("courseId", courseId);
        questionDeleteRequest.put("body", questionBody);

        JSONObject registerDeleteRequest = new JSONObject();
        registerDeleteRequest.put("function", "DeleteCourseRegistersCommand");
        registerDeleteRequest.put("queue", "user");
        JSONObject registerBody = new JSONObject();
        registerBody.put("courseId", courseId);
        registerDeleteRequest.put("body", registerBody);

        if (deletedCourse != null) {
            result.put("courseDeletedCount", 1);

            String pollCorrelationId = UUID.randomUUID().toString();
            String questionCorrelationId = UUID.randomUUID().toString();
            String registerCorrelationId = UUID.randomUUID().toString();

            try {
                MessageQueue.send(pollDeleteRequest.toString(), "pollReq", pollCorrelationId);
                MessageQueue.send(questionDeleteRequest.toString(), "questionReq", questionCorrelationId);
                MessageQueue.send(registerDeleteRequest.toString(), "userReq", registerCorrelationId);

                final BlockingQueue<String> pollResponse = new ArrayBlockingQueue<>(1);
                final BlockingQueue<String> questionResponse = new ArrayBlockingQueue<>(1);
                final BlockingQueue<String> registerResponse = new ArrayBlockingQueue<>(1);

                MessageQueue.channel.basicConsume("pollRes", false, (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(pollCorrelationId)) {
                        MessageQueue.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        pollResponse.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
                        MessageQueue.channel.basicCancel(consumerTag);
                    } else {
                        MessageQueue.channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                }, consumerTag -> {
                });

                MessageQueue.channel.basicConsume("questionRes", false, (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(questionCorrelationId)) {
                        MessageQueue.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        questionResponse.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
                        MessageQueue.channel.basicCancel(consumerTag);
                    } else {
                        MessageQueue.channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                }, consumerTag -> {
                });

                MessageQueue.channel.basicConsume("userRes", false, (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(registerCorrelationId)) {
                        MessageQueue.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        registerResponse.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
                        MessageQueue.channel.basicCancel(consumerTag);
                    } else {
                        MessageQueue.channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                }, consumerTag -> {
                });

                JSONObject registerObject = new JSONObject(registerResponse.take());
                JSONObject questionObject = new JSONObject(questionResponse.take());
                JSONObject pollObject = new JSONObject(pollResponse.take());

                result.put("pollDeletedCount", pollObject.get("pollDeletedCount"));
                result.put("questionDeletedCount", questionObject.get("questionDeletedCount"));
                result.put("registerDeletedCount", registerObject.get("registerDeletedCount"));

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            result.put("error", "Error deleting course");
        }
        System.out.println("======================"+result);
        return result;
    }
}
