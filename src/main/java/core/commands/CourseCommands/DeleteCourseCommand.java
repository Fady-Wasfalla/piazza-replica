package core.commands.CourseCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.result.DeleteResult;
import core.CommandDP;
import core.commands.PollCommands.DeleteCoursePollsCommand;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.sql.SQLOutput;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DeleteCourseCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        String[] schema = {"courseId"};

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String courseId = data.getString("courseId");
        Document deletedCourse = mongoDB.deleteOne(this.mongoClient, Collections.course,
                new Document("_id", new ObjectId(courseId)), jedis, "_id");

        JSONObject pollDeleteResquest = new JSONObject();
        pollDeleteResquest.put("function", "DeleteCoursePollsCommand");
        pollDeleteResquest.put("queue", "poll");
        JSONObject pollBody = new JSONObject();
        pollBody.put("courseId", courseId);
        pollDeleteResquest.put("body", pollBody);

        JSONObject questionDeleteResquest = new JSONObject();
        questionDeleteResquest.put("function", "DeleteCourseQuestionsCommand");
        questionDeleteResquest.put("queue", "question");
        JSONObject questionBody = new JSONObject();
        questionBody.put("courseId", courseId);
        questionDeleteResquest.put("body", questionBody);

        JSONObject registerDeleteResquest = new JSONObject();
        registerDeleteResquest.put("function", "DeleteCourseRegistersCommand");
        registerDeleteResquest.put("queue", "user");
        JSONObject registerBody = new JSONObject();
        registerBody.put("courseId", courseId);
        registerDeleteResquest.put("body", registerBody);
        
        if(deletedCourse != null){
            result.put("courseDeletedCount", 1);

            String pollCorrelationId = UUID.randomUUID().toString();
            String questionCorrelationId = UUID.randomUUID().toString();
            String registerCorrelationId = UUID.randomUUID().toString();

            try{
                NettyServerHandler.sendMessageToActiveMQ(pollDeleteResquest.toString(),"pollReq", pollCorrelationId);
                NettyServerHandler.sendMessageToActiveMQ(questionDeleteResquest.toString(),"questionReq", questionCorrelationId);
                NettyServerHandler.sendMessageToActiveMQ(registerDeleteResquest.toString(),"userReq", registerCorrelationId);

                final BlockingQueue<String> pollResponse = new ArrayBlockingQueue<>(1);         
                final BlockingQueue<String> questionResponse = new ArrayBlockingQueue<>(1);
                final BlockingQueue<String> registerResponse = new ArrayBlockingQueue<>(1);

                NettyHTTPServer.channel.basicConsume("pollRes", false, (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(pollCorrelationId)) {
                        NettyHTTPServer.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        pollResponse.offer(new String(delivery.getBody(), "UTF-8"));
                        NettyHTTPServer.channel.basicCancel(consumerTag);
                    }else{
                        NettyHTTPServer.channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                }, consumerTag -> {
                });

                NettyHTTPServer.channel.basicConsume("questionRes", false, (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(questionCorrelationId)) {
                        NettyHTTPServer.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        questionResponse.offer(new String(delivery.getBody(), "UTF-8"));
                        NettyHTTPServer.channel.basicCancel(consumerTag);
                    }else{
                        NettyHTTPServer.channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                }, consumerTag -> {
                });

                NettyHTTPServer.channel.basicConsume("userRes", false, (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(registerCorrelationId)) {
                        NettyHTTPServer.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        registerResponse.offer(new String(delivery.getBody(), "UTF-8"));
                        NettyHTTPServer.channel.basicCancel(consumerTag);
                    }else{
                        NettyHTTPServer.channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                }, consumerTag -> {
                });

                JSONObject registerObject = new JSONObject(registerResponse.take());
                JSONObject questionObject = new JSONObject(questionResponse.take());
                JSONObject pollObject = new JSONObject(pollResponse.take());

                result.put("pollDeletedCount", pollObject.get("pollDeletedCount"));
                result.put("questionDeletedCount", questionObject.get("questionDeletedCount"));
                result.put("registerDeletedCount", registerObject.get("registerDeletedCount"));

            } catch(Exception e) {
                e.printStackTrace();
            }

        } else{
          result.put("error", "Error deleting course");
        }
        System.out.println("======================"+result);
        return result;
    }
}
