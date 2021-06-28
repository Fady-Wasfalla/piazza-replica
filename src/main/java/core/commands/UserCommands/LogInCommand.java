package core.commands.UserCommands;

import Services.PostgreSQL;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import core.CommandDP;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import com.auth0.jwt.algorithms.Algorithm;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LogInCommand extends CommandDP {
    JSONObject result = new JSONObject();

    @Override
    public JSONObject execute() {

        String[] schema = {
                "email",
                "password"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        try {
            JSONObject user = PostgreSQL.getUser(data.getString("email"), data.getString("password"));

            // Expiry date (after 2 weeks)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.WEEK_OF_YEAR, 2);

            // Generate payload
            HashMap<String, String> payload = new HashMap<String, String>();
            payload.put("userName", user.getString("userName"));
            payload.put("role", user.getString("role"));

            // Generate token
            Dotenv dotenv = Dotenv.load();
            Algorithm algorithm = Algorithm.HMAC256(dotenv.get("secretToken"));
            String token = JWT.create()
                    .withExpiresAt(calendar.getTime())
                    .withPayload(payload)
                    .sign(algorithm);

            result.put("token", token);
            schema = null;
            user = null;
            calendar = null;
            payload = null;
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.put("error", "Wrong username or password!");
            return result;
        }
    }


}
