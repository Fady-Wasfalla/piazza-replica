package core.commands.UserCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import core.CommandDP;
import org.bson.BsonDocument;
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
        String[] updateKeys = {
                "bannerUserName",
                "banExpiryDate"
        };
        BsonDocument updateOperation = new BsonDocument();

//        String pollId= data.getString("pollId");

        String set = "{ $set: {";
        set+= "\"banned\":" + true +",";
        for (String key:updateKeys) {

            if(this.data.has(key)){
                set+= "\""+key+"\": \"" + this.data.getString(key)+"\",";
            }


        }
        set = set.substring(0,set.length()-1);
        set +=  "      } }" ;
        System.out.println(set);
        String courseId = this.data.getString("courseId");
        String userName = this.data.getString("userName");

        Document filterDocument = new Document("role", "student").append("courseId",courseId).append("userName",userName);

        Document resultDocument = mongoDB.update(mongoClient, Collections.register,filterDocument
                , BsonDocument.parse(set), new FindOneAndUpdateOptions(), jedis ,"courseId");


        if (resultDocument != null)
        {
            result.put("Status", "200 OK: Student banned successfully");
        }
        else {
            result.put("Status", "400 Error");
        }


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
}
