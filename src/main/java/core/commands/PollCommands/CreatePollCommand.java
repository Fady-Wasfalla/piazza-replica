package core.commands.PollCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import Notifications.Notifications;
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
        System.out.println("Execute Poll Create");
        String[] schema = {
                "courseId",
                "userName",
                "options",
                "expiryDate",
                "title"
        };

        if (!validateJSON(schema, data)) {
            System.out.println("Invalid Json");
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("answers", new JSONArray());
        this.data.put("createdAt", new Date().getTime() + "");

        Document pollDocument = Document.parse(data.toString());

        BsonValue pollId = mongoDB.create(mongoClient, Collections.poll, pollDocument, jedis, "_id")
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
            NettyServerHandler.sendMessageToActiveMQ(notificationRequest.toString(),requestQueue,correlationId);
            final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
            NettyHTTPServer.channel.basicConsume(responseQueue, false, (consumerTag, delivery) -> {

                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    NettyHTTPServer.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    response.offer(new String(delivery.getBody(), "UTF-8"));
                    NettyHTTPServer.channel.basicCancel(consumerTag);
                }else{
                    NettyHTTPServer.channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                }
            }, consumerTag -> {
            });

            String notificationResponse = response.take();
            JSONObject notificationObject = new JSONObject(notificationResponse);
            System.out.println(notificationObject);



        } catch (Exception e){
            e.printStackTrace();
        }


        return result;
    }

//    private void sendNotification(BsonValue pollId, String courseId) {
//
//        Document filterDocument = new Document("role", "student").append("courseId",courseId).append("banned",false);
//        ArrayList<Document> students = mongoDB.read(mongoClient,Collections.register,filterDocument);
//
//        for (Document d:students) {
//
//            String username = d.getString("userName");
//            JSONObject notification = new JSONObject();
//            notification.put("userName",username);
//            notification.put("description","An instructor posted a new poll");
//            notification.put("model",pollId.asObjectId().getValue().toString());
//            notification.put("onModel","Poll");
//            notification.put("createdAt", new Date().getTime() + "");
//
//            Document notificationDocument = Document.parse(notification.toString());
//
//            BsonValue notificationId = mongoDB.create(mongoClient, Collections.notification, notificationDocument).getInsertedId();
//
//            Document tokenFilterDocument = new Document("userName", username);
//            ArrayList<Document> token = mongoDB.read(mongoClient, Collections.token, tokenFilterDocument);
//            if(token.size()>0){
//                Notifications notify = new Notifications();
//                try {
//                    notify.notify(token.get(0).getString("token"),"An instructor posted a new poll");
////                    notify.notify("d3-GpfzqP5WOaJBfZB05yP:APA91bFOtEuWyXTvYcSZQI0eWhTu48IuncorBWpLyHXVdUoUMFt8d7lR5OudjOH2RiUjch47obFj_G4tDGTRTBmfCZhNzkNCLce_KhWJnhDD-5wglEJdThCS4Ps53KCpA_qGRjbwn0SP","A student asked a new question");
//
//                } catch (Exception e) {
//
//                }
//            }
//        }
//
//
//    }


}
