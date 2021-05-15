package core.commands.CourseCommands;

import core.CommandDP;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewCourseCommand extends CommandDP {
    JSONObject result = new JSONObject();
    
    @Override
    public JSONObject execute() {

        String[] schema = {"_id"};

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }



        return null;
    }
}
