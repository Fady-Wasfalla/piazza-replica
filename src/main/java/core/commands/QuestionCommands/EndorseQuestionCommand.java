package core.commands.QuestionCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import static Services.mongoDB.getCollection;
import static com.mongodb.client.model.Updates.*;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EndorseQuestionCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {
                "questionId",
                "userName",
                "description" ,
                "media"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String questionId= this.data.getString("questionId");
        Document myQuestion = mongoDB.readOne(mongoClient, Collections.question, new Document("_id", new ObjectId(questionId)),jedis,"_id");


        MongoCollection<Document> collection = getCollection(mongoClient, Collections.question);

//        JSONObject x1 = new JSONObject();
//        x1.put("questionId",questionId);
//        JSONObject x2 = new JSONObject();
//        x2.put("userName",this.data.getString("userName"));
//        x2.put("description",this.data.getString("description"));
//        x1.put("answers",x2);
//
//        Document x3 = Document.parse(x1.toString());
//        Document projection = new Document("_id",new ObjectId(questionId))
//                .append("$elemMatch", new Document("answers",
//                new Document("userName", this.data.getString("userName"))
//                        .append("description",this.data.getString("description"))) )
//                ;
//
//        ArrayList<Document> x =  mongoDB.read(this.mongoClient, Collections.question,
//                projection);
//        System.out.println(projection);
//        System.out.println(x);

        JSONObject newAnswer= new JSONObject();
        newAnswer.put("username", data.getString("userName"));
        newAnswer.put("description", data.getString("description"));
        newAnswer.put("media", data.getJSONArray("media"));

        Document finalAns = Document.parse(newAnswer.toString());
//        UpdateResult resultDocument = mongoDB.update(mongoClient, Collections.question,
//                new Document("_id", new ObjectId(questionId)) ,set("answers", true), new UpdateOptions());


        UpdateResult resultDocument = collection.updateOne(new Document("_id", new ObjectId(questionId)),
                new Document().append("$pull", new Document("answers",finalAns)
                ));
        if(resultDocument.getModifiedCount() == 0){
            result.put("error","no such answer to this question");
            return result;
        }

        newAnswer.put("endorsed", true);
        finalAns = Document.parse(newAnswer.toString());
        resultDocument = collection.updateOne(new Document("_id", new ObjectId(questionId)),
                new Document().append("$push", new Document("answers",finalAns)
                ));

        long modifiedQuestionsCount = resultDocument.getModifiedCount();
        result.put("Modified Questions Count", modifiedQuestionsCount);

        String correlationId = UUID.randomUUID().toString();
        String requestQueue = "notificationReq";
        String responseQueue = "notificationRes";

        JSONObject notificationRequest = new JSONObject();
        notificationRequest.put("queue", "notification");
        notificationRequest.put("function", "NotifyStudentCommand");

        JSONObject body = new JSONObject();
        body.put("userName", myQuestion.getString("userName"));
        body.put("description","Your answer was endorsed");
        body.put("model",  this.data.getString("questionId"));
        body.put("onModel", "Question");
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
