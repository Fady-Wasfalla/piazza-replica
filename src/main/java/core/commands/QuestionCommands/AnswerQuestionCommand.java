package core.commands.QuestionCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Command;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import com.mongodb.client.model.UpdateOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static Services.mongoDB.getCollection;
import static com.mongodb.client.model.Updates.set;

public class AnswerQuestionCommand extends CommandDP {

    @Override
    public JSONObject execute() {
        JSONObject result= new JSONObject();
        String [] schema= {
                "questionId",
                "userName",
                "description",
                "endorsed",
                "media",
                "sort",
                "skip",
                "limit"
        };
        System.out.println(data);
        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }
        String questionId= this.data.getString("questionId");
        int skip = this.data.getInt("skip");
        int limit = this.data.getInt("limit");
        String sort = this.data.getString("sort");

        if(sort == null){
            sort = "title";
        }

        ArrayList<Document> myQuestions = mongoDB.readAll(mongoClient, Collections.question,
                new Document("_id", new ObjectId(questionId)), Sorts.ascending(sort), skip, limit, jedis);

        Document myQuestion;
        if(!(myQuestions.size()==0)){
            myQuestion= (Document) myQuestions.get(0);
        }
        else{
            result.put("error","no Questions with such ID");
            return result;
        }
        System.out.println(myQuestion);
//        ArrayList<JSONObject> answers = (ArrayList<JSONObject>) myQuestion.get("answers");


//        Object [] newAnswersArray = new Object [answers.length+1];
//        ArrayList<Object> myAnswers = new ArrayList<Object>();
//        for(int i=0; i<answers.length;i++){
//            myAnswers.add(answers[i]);
//            newAnswersArray[i]=answers[i];
//        }
        JSONObject newAnswer= new JSONObject();
        newAnswer.put("username", data.getString("userName"));
        newAnswer.put("description", data.getString("description"));
        newAnswer.put("endorsed", data.get("endorsed"));
        newAnswer.put("media", data.get("media"));

        Document finalAns = Document.parse(newAnswer.toString());
//        answers.add(newAnswer);
//        System.out.println(answers);

//        newAnswersArray[newAnswersArray.length-1]=newAnswer;

//        JSONObject[] finalAnswers = new JSONObject[answers.size()];
//        finalAnswers = answers.toArray(finalAnswers);

//        System.out.println(answers);
//        UpdateResult resultDocument = mongoDB.update(mongoClient, Collections.question,
//                new Document("_id", new ObjectId(questionId)) ,pushEach("answers", answers), new UpdateOptions());

        MongoCollection<Document> collection = getCollection(mongoClient, Collections.question);
        UpdateResult resultDocument = collection.updateOne(new Document("_id", new ObjectId(questionId)),
                new Document().append("$push", new Document("answers",finalAns)
        ));

//
        long modifiedQuestionsCount= resultDocument.getModifiedCount();
        result.put("Modified Questions Count", modifiedQuestionsCount);


        String correlationId = UUID.randomUUID().toString();
        String requestQueue = "notificationReq";
        String responseQueue = "notificationRes";

        JSONObject notificationRequest = new JSONObject();
        notificationRequest.put("queue", "notification");
        notificationRequest.put("function", "NotifyStudentCommand");

        JSONObject body = new JSONObject();
        body.put("userName", myQuestion.getString("userName"));
        body.put("description","Your question have a new answer");
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
