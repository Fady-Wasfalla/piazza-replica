package core.commands.UserCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import static com.mongodb.client.model.Updates.*;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static Services.mongoDB.getCollection;

public class BanStudentCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        JSONObject result= new JSONObject();
        String [] schema= {
                "courseId",
                "userName" ,
                "banExpiryDate" ,
                "bannerUserName"
        };

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }
        String courseId = this.data.getString("courseId");
        String userName = this.data.getString("userName");
        String banExpiryDate = this.data.getString("banExpiryDate");
        String bannerUserName = this.data.getString("bannerUserName");

        Document filterDocument = new Document("role", "student").append("courseId",courseId).append("userName",userName);

        UpdateResult resultDocument = mongoDB.update(mongoClient,Collections.register,
                filterDocument,set("banned",true), new UpdateOptions());

        resultDocument = mongoDB.update(mongoClient,Collections.register,
                filterDocument,set("banExpiryDate",banExpiryDate), new UpdateOptions());

        resultDocument = mongoDB.update(mongoClient,Collections.register,
                filterDocument,set("bannerUserName",bannerUserName), new UpdateOptions());

        long modifiedBanCount = resultDocument.getModifiedCount();
        result.put("Modified Ban Count", modifiedBanCount);

        String correlationId = UUID.randomUUID().toString();
        String requestQueue = "notificationReq";
        String responseQueue = "notificationRes";

        JSONObject notificationRequest = new JSONObject();
        notificationRequest.put("queue", "notification");
        notificationRequest.put("function", "NotifyStudentCommand");

        JSONObject body = new JSONObject();
        body.put("userName", userName);
        body.put("description","Your are banned");
        body.put("model",  courseId);
        body.put("onModel", "Course");

        notificationRequest.put("body", body);

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
}
