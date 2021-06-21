package core.commands.PollCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.model.Sorts;
import core.CommandDP;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewAllPollsCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {"courseId", "sort", "limit", "skip"};


        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String courseId = this.data.getString("courseId");
        int skip = this.data.getInt("skip");
        int limit = this.data.getInt("limit");
        String sort = this.data.getString("sort");

        if (sort == null) {
            sort = "title";
        }

        ArrayList<Document> queryResults = mongoDB.readAll(this.mongoClient, Collections.poll,
                new Document("courseId", courseId), Sorts.ascending(sort), skip, limit, jedis);

        if (queryResults.isEmpty()) {
            result.put("[]", "No polls to show for this course");
            return result;
        }

        for (Document doc : queryResults) {
            JSONObject instance = new JSONObject(doc.toJson().toString());
            result.append("poll", instance);
        }


        return result;
    }
}
