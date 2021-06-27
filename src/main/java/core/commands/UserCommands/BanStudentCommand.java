package core.commands.UserCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import RabbitMQ.MessageQueue;
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

        String set = "{ $set: {";
        set+= "\"banned\":" + true +",";
        for (String key:updateKeys) {

            if(this.data.has(key)){
                set+= "\""+key+"\": \"" + this.data.getString(key)+"\",";
            }


        }
        set = set.substring(0,set.length()-1);
        set +=  "      } }" ;
        String courseId = this.data.getString("courseId");
        String userName = this.data.getString("userName");

        Document filterDocument = new Document("role", "student").append("courseId",courseId).append("userName",userName);

        Document resultDocument = mongoDB.update(Collections.register,filterDocument
                , BsonDocument.parse(set), new FindOneAndUpdateOptions(),"courseId");


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
        set = null;
        courseId = null;
        userName = null;
        filterDocument = null;
        resultDocument = null;
        requestQueue = null;
        responseQueue = null;
        notificationRequest = null;
        body = null;

        return result;
    }
}
