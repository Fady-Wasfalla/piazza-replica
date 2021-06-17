package core.commands.UserCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.result.DeleteResult;
import core.CommandDP;
import org.bson.Document;
import org.json.JSONObject;

public class DeleteCourseRegistersCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {"courseId"};

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String courseId= data.getString("courseId");
        DeleteResult deletedRegisters = mongoDB.deleteMany(mongoClient, Collections.register, new Document("courseId", courseId));

        long registerDeletedCount = deletedRegisters.getDeletedCount();
        result.put("registerDeletedCount", registerDeletedCount);

        return result;
    }
}
