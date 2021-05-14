package core.commands.QuestionCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import core.CommandDP;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class CreateQuestionCommand extends CommandDP {


    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {
                "courseId",
                "userName",
                "title",
                "description",
                "anonymous",
                "private",
                "media"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("createdAt", new Date().getTime() + "");

        Document questionDocument = Document.parse(data.toString());

        BsonValue questionId = mongoDB.create(mongoClient, Collections.question, questionDocument)
                .getInsertedId();

        result.put("questionId", questionId.asObjectId().getValue().toString());
        return result;
    }

}
