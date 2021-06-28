package core.commands.UserCommands;

import Services.Collections;
import Services.PostgreSQL;
import Services.mongoDB;
import com.mongodb.client.result.InsertOneResult;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Date;

public class RegisterStudentEmailCommand extends CommandDP {
    JSONObject result = new JSONObject();

    @Override
    public JSONObject execute() throws SQLException {

        String[] schema = {
                "courseId",
                "email"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        data.put("banned", false);
        data.put("banExpiryDate", JSONObject.NULL);
        data.put("bannerUserName", JSONObject.NULL);
        data.put("createdAt", new Date().getTime() + "");
        JSONObject user = PostgreSQL.getUserByEmail(this.data.getString("email"));
        if(user == null)
            return result.put("status", "User not found");
        String userName = user.getString("userName");
        data.put("userName",userName);
        data.remove("email");

        Document registrationDocument = Document.parse(data.toString());

        InsertOneResult insertOneResult = mongoDB.create(Collections.register, registrationDocument, "_id");

        BsonValue registeredId = insertOneResult.getInsertedId();

        result.put("registeredId", registeredId.asObjectId().getValue().toString());
        schema = null;
        registrationDocument = null;
        insertOneResult = null;
        registeredId = null;
        return result;
    }
}
