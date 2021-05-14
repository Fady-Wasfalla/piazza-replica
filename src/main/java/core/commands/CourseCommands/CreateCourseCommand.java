package core.commands.CourseCommands;

import core.CommandDP;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CreateCourseCommand extends CommandDP {


    @Override
    public JSONObject execute() {

        // name, course, 
        String[] schema = {"name", };
        boolean valid = this.validateJSON(schema, this.data);

        return null;
    }
}
