package core.commands.UserCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.result.InsertOneResult;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONObject;

public class RegisterUserCommand extends CommandDP {
    JSONObject result = new JSONObject();

    @Override
    public JSONObject execute() {

        String[] schema = {
                "courseId",
                "userName",
                "role",
                "banned",
                "banExpiryDate",
                "bannerUserName",
                "createdAt"
        };

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        Document registrationDocument = Document.parse(data.toString());

        InsertOneResult insertOneResult = mongoDB.create(this.mongoClient, Collections.register,registrationDocument);

        BsonValue registeredId = insertOneResult.getInsertedId();

        result.put("registeredId", registeredId.asObjectId().getValue().toString());
        return result;
    }
}
