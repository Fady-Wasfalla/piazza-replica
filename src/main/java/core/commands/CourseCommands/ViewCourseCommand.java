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
        System.out.println(data);
        System.out.println("=============================");
        System.out.println(schema);
        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String courseId = this.data.getString("courseId");

        Document queryResult = mongoDB.readOne(this.mongoClient, Collections.course,
                new Document("_id", new ObjectId(courseId)), jedis, "_id");

        if(queryResult.isEmpty()) {
            result.put("[]", "Invalid Course ID");
            return result;
        }

        System.out.println("Monica ==> " + queryResult);

        JSONObject result = new JSONObject(queryResult.toJson().toString());
        return result;
    }
}
