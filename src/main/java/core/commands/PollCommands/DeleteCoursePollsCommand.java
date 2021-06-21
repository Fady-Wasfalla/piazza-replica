package core.commands.PollCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.result.DeleteResult;
import core.CommandDP;
import org.bson.Document;
import org.json.JSONObject;

public class DeleteCoursePollsCommand extends CommandDP {

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {"courseId"};

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String courseId = data.getString("courseId");

        DeleteResult deletedPolls = mongoDB.deleteMany(mongoClient, Collections.poll, new Document("courseId", courseId), jedis);

        long pollDeletedCount = deletedPolls.getDeletedCount();
        result.put("pollDeletedCount", pollDeletedCount);

        return result;

    }
}
