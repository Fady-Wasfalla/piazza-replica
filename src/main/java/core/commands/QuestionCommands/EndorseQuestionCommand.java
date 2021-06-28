package core.commands.QuestionCommands;

import NettyHTTP.NettyHTTPServer;
import NettyHTTP.NettyServerHandler;
import RabbitMQ.MessageQueue;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import core.CommandDP;
import org.bson.BsonDocument;
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

        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String questionId= this.data.getString("questionId");
        String set = "{ $set: { \"endorsed\":true }}";
        Document updatedQuestion = mongoDB.update(Collections.question, new Document("_id", new ObjectId(questionId)), BsonDocument.parse(set),new FindOneAndUpdateOptions(),"_id");

        result.put("Endorsed Question", updatedQuestion);


        schema = null;
        questionId = null;

        return result;

    }
}
