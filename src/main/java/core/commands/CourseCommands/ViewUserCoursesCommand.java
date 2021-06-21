package core.commands.CourseCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.model.Sorts;
import core.CommandDP;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewUserCoursesCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {"userName", "skip", "limit", "sort"};

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String userName = this.data.getString("userName");
        int skip = this.data.getInt("skip");
        int limit = this.data.getInt("limit");
        String sort = this.data.getString("sort");

        if (sort == null) {
            sort = "name";
        }

        ArrayList<Document> queryResults = mongoDB.readAll(this.mongoClient, Collections.register,
                new Document("userName", userName), Sorts.ascending(sort), skip, limit, jedis);

        if (queryResults.isEmpty()) {
            result.put("[]", "User not registered in any courses");
            return result;
        }

        for (Document doc : queryResults) {
            JSONObject instance = new JSONObject(doc.toJson().toString());
            result.append("course", instance);
        }

        return result;
    }
}
