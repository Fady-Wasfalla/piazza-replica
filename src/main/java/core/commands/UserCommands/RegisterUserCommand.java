package core.commands.UserCommands;

import core.CommandDP;
import org.json.JSONObject;

public class RegisterUserCommand extends CommandDP {


    @Override
    public JSONObject execute() {

        JSONObject result = new JSONObject("{id:RMY}");
        return result;
    }
}
