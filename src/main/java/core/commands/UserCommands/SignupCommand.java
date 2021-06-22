package core.commands.UserCommands;

import core.CommandDP;
import org.json.JSONObject;

public class SignupCommand extends CommandDP {


    @Override
    public JSONObject execute() {
        System.out.println(this.data.toString());
        JSONObject result = new JSONObject();
        result.put("response", this.data.toString());
        return result;
    }


}
