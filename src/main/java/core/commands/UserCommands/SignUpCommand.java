package core.commands.UserCommands;

import Services.PostgreSQL;
import core.CommandDP;
import org.json.JSONObject;

import java.sql.SQLException;

public class SignUpCommand extends CommandDP {
    JSONObject result = new JSONObject();

    @Override
    public JSONObject execute() {

        String[] schema = {
                "userName",
                "firstName",
                "lastName",
                "email",
                "password",
                "role",
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        try {
            int id = PostgreSQL.registerUser(data);
            result.put("userId", id);
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.put("error", "Cannot insert user (SQL)");
            return result;
        }
    }


}
