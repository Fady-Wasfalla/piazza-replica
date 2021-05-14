package core.commands.CourseCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import RabbitMQ.Producer;
import Services.mongoDB;
import com.mongodb.client.result.InsertOneResult;
import core.CommandDP;
import org.bson.BsonObjectId;
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
        String[] schema = {
                "name",
                "userName",
                "code",
        };

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("createdAt", new Date().getTime()+"");

        BsonValue courseId = mongoDB.create(mongoClient, "course", new Document())
                .getInsertedId();

        result.put("courseId", courseId.asObjectId().getValue().toString());

        String correlationId = UUID.randomUUID().toString();
        String requestQueue = "userReq";
        String responseQueue = "userRes";

        JSONObject registerRequest = new JSONObject();
        registerRequest.put("courseId", courseId);
        registerRequest.put("userName",data.getString("userName"));
        registerRequest.put("role", "instructor");
        registerRequest.put("banned", false);
        registerRequest.put("banExpiryDate",JSONObject.NULL);
        registerRequest.put("bannerUserName",JSONObject.NULL);
        registerRequest.put("createdAt", this.data.get("createdAt"));
        registerRequest.put("queue", "user");
        registerRequest.put("function", "RegisterUserCommand");

        try{
            NettyServerHandler.sendMessageToActiveMQ(registerRequest.toString(),requestQueue,correlationId);
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

            String registerResponse = response.take();
            System.out.println("Paul ===> " + registerResponse);
            JSONObject registerObject = new JSONObject(registerResponse);

            // to be changed
            ObjectId id = new ObjectId(registerObject.get("registeredId").toString());
            System.out.println("Monica" + id);
            result.put("registeredId", id.toString());

        } catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }
}
