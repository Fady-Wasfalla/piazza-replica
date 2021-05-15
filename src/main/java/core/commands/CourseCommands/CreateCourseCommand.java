package core.commands.CourseCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import RabbitMQ.Producer;
import Services.Collections;
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

        Document courseDocument = Document.parse(data.toString());

        BsonValue courseId = mongoDB.create(mongoClient, Collections.course, courseDocument)
                .getInsertedId();

        result.put("courseId", courseId.asObjectId().getValue().toString());

        String correlationId = UUID.randomUUID().toString();
        String requestQueue = "userReq";
        String responseQueue = "userRes";

        JSONObject registerRequest = new JSONObject();
        registerRequest.put("queue", "user");
        registerRequest.put("function", "RegisterUserCommand");

        JSONObject body = new JSONObject();
        body.put("courseId", courseId);
        body.put("userName",data.getString("userName"));
        body.put("role", "instructor");
        
        registerRequest.put("body", body);

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
            JSONObject registerObject = new JSONObject(registerResponse);

            // to be changed
            ObjectId id = new ObjectId(registerObject.get("registeredId").toString());

            result.put("registeredId", id.toString());

        } catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }
}
