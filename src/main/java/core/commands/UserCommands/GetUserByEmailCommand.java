package core.commands.UserCommands;

import Services.PostgreSQL;
import core.CommandDP;
import org.json.JSONObject;
import java.sql.SQLException;

public class GetUserByEmailCommand extends CommandDP {
    JSONObject result = new JSONObject();

    public JSONObject execute() {

        String[] schema = {
                "email"
        };
        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }
        try {
            JSONObject user = PostgreSQL.getUserByEmail(data.getString("email"));
            return user;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.put("error", "Wrong email!");
            return result;
        }
    }
}
