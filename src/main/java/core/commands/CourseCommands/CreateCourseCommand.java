package core.commands.CourseCommands;

import RabbitMQ.MessageQueue;
import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CreateCourseCommand extends CommandDP {
    JSONObject result = new JSONObject();

    @Override
    public JSONObject execute() {

        System.out.println("from create course" + this.user);

        String[] schema = {
                "name",
                "userName",
                "code",
        };
        data = (JSONObject) data.get("body");
        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("createdAt", new Date().getTime() + "");

        Document courseDocument = Document.parse(data.toString());

        BsonValue courseId = mongoDB.create(mongoClient, Collections.course, courseDocument, jedis, "_id")
                .getInsertedId();

        result.put("courseId", courseId.asObjectId().getValue().toString());

        String correlationId = UUID.randomUUID().toString();
        String requestQueue = "userReq";
        String responseQueue = "userRes";

        JSONObject registerRequest = new JSONObject();
        registerRequest.put("queue", "user");
        registerRequest.put("function", "RegisterUserCommand");

        JSONObject body = new JSONObject();
        body.put("courseId", courseId.asObjectId().getValue().toString());
        body.put("userName", data.getString("userName"));
        body.put("role", "instructor");

        registerRequest.put("body", body);
        registerRequest.put("user", user);

        try {
            System.out.println("Create Course Command: " + registerRequest);
            MessageQueue.send(registerRequest.toString(), requestQueue, correlationId);
            final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
            MessageQueue.channel.basicConsume(responseQueue, false, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    MessageQueue.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    response.offer(new String(delivery.getBody(), "UTF-8"));
                    MessageQueue.channel.basicCancel(consumerTag);
                } else {
                    MessageQueue.channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                }
            }, consumerTag -> {
            });

            String registerResponse = response.take();
            JSONObject registerObject = new JSONObject(registerResponse);

            // to be changed
            ObjectId id = new ObjectId(registerObject.get("registeredId").toString());

            result.put("registeredId", id.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}