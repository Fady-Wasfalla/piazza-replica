package core.commands.PollCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import Notifications.Notifications;
import RabbitMQ.MessageQueue;
import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CreatePollCommand extends CommandDP {

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        String[] schema = {
                "courseId",
                "userName",
                "options",
                "expiryDate",
                "title"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("answers", new JSONArray());
        this.data.put("createdAt", new Date().getTime() + "");

        Document pollDocument = Document.parse(data.toString());

        BsonValue pollId = mongoDB.create(Collections.poll, pollDocument, "_id")
                .getInsertedId();

        result.put("pollId", pollId.asObjectId().getValue().toString());

        //
        String correlationId = UUID.randomUUID().toString();
        String requestQueue = "notificationReq";
        String responseQueue = "notificationRes";

        JSONObject notificationRequest = new JSONObject();
        notificationRequest.put("queue", "notification");
        notificationRequest.put("function", "NotifyAllStudentsCommand");

        JSONObject body = new JSONObject();
        body.put("userName", this.data.getString("userName"));

        body.put("courseId", this.data.getString("courseId"));
        body.put("description","An Instructor created a new poll");
        body.put("model", pollId.asObjectId().getValue().toString());
        body.put("onModel", "Poll");
        body.put("sort", "_id");
        body.put("skip", 0);
        body.put("limit", 0);

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
        pollDocument = null;
        pollId = null;
        requestQueue = null;
        responseQueue = null;
        notificationRequest = null;
        body = null;
        return result;
    }

}
