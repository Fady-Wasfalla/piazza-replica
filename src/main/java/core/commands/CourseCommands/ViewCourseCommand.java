package core.commands.CourseCommands;

import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewCourseCommand extends CommandDP {
    JSONObject result = new JSONObject();
    
    @Override
    public JSONObject execute() {

        String[] schema = {"courseId"};

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String courseId = this.data.getString("courseId");

        ArrayList<Document> queryResults = mongoDB.read(this.mongoClient, Collections.course,
                new Document("_id", new ObjectId(courseId)));

        if(queryResults.isEmpty()) {
            result.put("[]", "Invalid Course ID");
            return result;
        }

        System.out.println("Monica ==> " + queryResults.get(0));

        JSONObject result = new JSONObject(queryResults.get(0).toJson().toString());
        return result;
    }
}
