package core.commands.UserCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.result.InsertOneResult;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONObject;

import java.util.Date;

public class RegisterUserCommand extends CommandDP {
    JSONObject result = new JSONObject();

    @Override
    public JSONObject execute() {

        String[] schema = {
                "courseId",
                "userName",
                "role",
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        data.put("banned", false);
        data.put("banExpiryDate", JSONObject.NULL);
        data.put("bannerUserName", JSONObject.NULL);
        data.put("createdAt", new Date().getTime() + "");

        Document registrationDocument = Document.parse(data.toString());

        InsertOneResult insertOneResult = mongoDB.create(Collections.register, registrationDocument, jedis, "_id");

        BsonValue registeredId = insertOneResult.getInsertedId();

        result.put("registeredId", registeredId.asObjectId().getValue().toString());
        return result;
    }
}
