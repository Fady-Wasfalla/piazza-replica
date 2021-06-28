package core.commands.QuestionCommands;

import RabbitMQ.MessageQueue;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static Services.mongoDB.getCollection;

public class EndorseAnswerCommand extends CommandDP {
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
        Document myQuestion = mongoDB.readOne(Collections.question, new Document("_id", new ObjectId(questionId)),"_id");


        MongoCollection<Document> collection = getCollection(Collections.question);

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
        questionId = null;
        myQuestion = null;
        collection = null;
        newAnswer = null;
        finalAns = null;
        resultDocument = null;
        requestQueue = null;
        responseQueue = null;
        notificationRequest = null;
        body = null;

        return result;

    }
}
