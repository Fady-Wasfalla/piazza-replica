package core.commands.QuestionCommands;

import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

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
                "media",
                "likes",
                "answers"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("createdAt", new Date().getTime() + "");

        String description = this.data.getString("description").toLowerCase(Locale.ROOT);
        this.data.put("description", description);

        Document questionDocument = Document.parse(data.toString());

        BsonValue questionId = mongoDB.create(mongoClient, Collections.question, questionDocument, jedis, "_id")
                .getInsertedId();

        result.put("questionId", questionId.asObjectId().getValue().toString());
        return result;
    }

}
