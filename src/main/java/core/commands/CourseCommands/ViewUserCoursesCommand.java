package core.commands.CourseCommands;

import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewUserCoursesCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {"userName"};

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String userName = this.data.getString("userName");

        ArrayList<Document> queryResults = mongoDB.read(this.mongoClient, Collections.register,
                new Document("userName", userName));

        if(queryResults.isEmpty()) {
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
