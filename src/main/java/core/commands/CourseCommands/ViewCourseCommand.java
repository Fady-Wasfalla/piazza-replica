package core.commands.CourseCommands;

import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

public class ViewCourseCommand extends CommandDP {
    JSONObject result = new JSONObject();

    @Override
    public JSONObject execute() {

        String[] schema = {"courseId"};

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String courseId = this.data.getString("courseId");

        Document queryResult = mongoDB.readOne(Collections.course, new Document("_id", new ObjectId(courseId)), "_id");

        if (queryResult.isEmpty()) {
            result.put("[]", "Invalid Course ID");
            return result;
        }
        JSONObject result = new JSONObject(queryResult.toJson());
        schema = null;
        courseId = null;
        queryResult = null;
        return result;
    }
}
