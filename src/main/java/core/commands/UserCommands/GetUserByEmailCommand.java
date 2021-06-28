package core.commands.UserCommands;

import Services.PostgreSQL;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import core.CommandDP;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

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
