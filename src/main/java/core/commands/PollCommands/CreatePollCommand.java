package core.commands.PollCommands;

import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

public class CreatePollCommand extends CommandDP {

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        System.out.println("Execute Poll Create");
        String[] schema = {
                "courseId",
                "userName",
                "options",
                "expiryDate",
                "title"
        };

        if (!validateJSON(schema, data)) {
            System.out.println("Invalid Json");
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("answers", new JSONArray());
        this.data.put("createdAt", new Date().getTime() + "");

        Document pollDocument = Document.parse(data.toString());

        BsonValue pollId = mongoDB.create(Collections.poll, pollDocument, jedis, "_id")
                .getInsertedId();

        result.put("pollId", pollId.asObjectId().getValue().toString());
        return result;
    }

}
