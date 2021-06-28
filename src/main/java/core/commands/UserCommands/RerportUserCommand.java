package core.commands.UserCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.result.InsertOneResult;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONObject;

import java.util.Date;

public class RerportUserCommand extends CommandDP {
    JSONObject result = new JSONObject();

    @Override
    public JSONObject execute() {

        String[] schema = {
                "courseId",
                "reporterUserName",
                "reason",
                "reportedUserName"
                
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        data.put("createdAt", new Date().getTime() + "");

        Document reportDocument = Document.parse(data.toString());

        InsertOneResult insertOneResult = mongoDB.create(Collections.report, reportDocument, "_id");

        BsonValue reportdId = insertOneResult.getInsertedId();

        result.put("reportId", reportdId.asObjectId().getValue().toString());
        schema = null;
        reportDocument = null;
        insertOneResult = null;
        reportdId = null;
        return result;
    }
}
