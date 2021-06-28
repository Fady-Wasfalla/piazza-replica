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

public class DeleteAnswerCommand extends CommandDP {
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
        long modifiedQuestionsCount = resultDocument.getModifiedCount();
        result.put("Modified Questions Count", modifiedQuestionsCount);



        schema = null;
        questionId = null;
        myQuestion = null;
        collection = null;
        newAnswer = null;
        finalAns = null;
        resultDocument = null;
   

        return result;

    }
}
