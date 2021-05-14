package core.commands.UserCommands;

import Services.mongoDB;
import com.mongodb.client.result.InsertOneResult;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;
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
        JSONObject registrationData= new JSONObject();
        registrationData.put("courseId", data.get("courseId"));
        registrationData.put("userName", data.get("userName"));
        registrationData.put("role", data.get("role"));
        registrationData.put("banned", data.get("banned"));
        registrationData.put("banExpiryDate", data.get("banExpiryDate"));
        registrationData.put("bannerUserName", data.get("bannerUserName"));
        registrationData.put("createdAt", data.get("createdAt"));

        Document registrationDocument = Document.parse(registrationData.toString());

        InsertOneResult insertOneResult = mongoDB.create(this.mongoClient,
                "register",registrationDocument);

        String registeredId = insertOneResult.getInsertedId().toString();

        result.put("registeredId", registeredId);
        return result;
    }
}
