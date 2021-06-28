package core.commands.QuestionCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import Notifications.Notifications;
import RabbitMQ.MessageQueue;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CreateQuestionCommand extends CommandDP {


    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {
                "courseId",
                "userName",
                "title",
                "description",
                "anonymous",
                "private",
                "media",
                "likes",
                "answers",
                "endorsed",
                "mentions"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("createdAt", new Date().getTime() + "");

        String description = this.data.getString("description").toLowerCase(Locale.ROOT);
        this.data.put("description", description);

        Document questionDocument = Document.parse(data.toString());

        BsonValue questionId = mongoDB.create(Collections.question, questionDocument, "_id")
                .getInsertedId();

        result.put("questionId", questionId.asObjectId().getValue().toString());


        String correlationId = UUID.randomUUID().toString();
        String requestQueue = "notificationReq";
        String responseQueue = "notificationRes";

        JSONObject notificationRequest = new JSONObject();
        notificationRequest.put("queue", "notification");
        notificationRequest.put("function", "NotifyAllStudentsCommand");

        JSONObject body = new JSONObject();
        body.put("userName", this.data.getString("userName"));

        body.put("courseId", this.data.getString("courseId"));
        body.put("description","A student asked a new question");
        body.put("model",  questionId.asObjectId().getValue().toString());
        body.put("onModel", "Question");
        body.put("sort", "role");
        body.put("skip", 0);
        body.put("limit", 100);

        notificationRequest.put("body", body);
        notificationRequest.put("user", this.user);

        try{
            MessageQueue.send(notificationRequest.toString(),requestQueue,correlationId);
            final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
            MessageQueue.channel.basicConsume(responseQueue, false, (consumerTag, delivery) -> {

                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    MessageQueue.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    response.offer(new String(delivery.getBody(), "UTF-8"));
                    MessageQueue.channel.basicCancel(consumerTag);
                }else{
                    MessageQueue.channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                }
            }, consumerTag -> {
            });

            String notificationResponse = response.take();
            JSONObject notificationObject = new JSONObject(notificationResponse);
            System.out.println(notificationObject);

        } catch (Exception e){
            e.printStackTrace();
        }

        schema = null;
        description = null;
        questionDocument = null;
        questionId = null;
        requestQueue = null;
        responseQueue = null;
        notificationRequest = null;
        body = null;

        return result;
    }

}
