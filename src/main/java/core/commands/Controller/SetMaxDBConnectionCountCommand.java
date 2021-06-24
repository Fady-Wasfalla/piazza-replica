package core.commands.Controller;

import Services.PostgreSQL;
import core.CommandDP;
import org.bson.json.JsonObject;
import org.json.JSONObject;

import java.sql.SQLException;

public class SetMaxDBConnectionCountCommand extends CommandDP {
    @Override
    public JSONObject execute() {

        JSONObject result = new JSONObject();

        String[] schema = {"maxConnections"};

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        int maxConnections = data.getInt("maxConnections");

        try {
            PostgreSQL.closeDBPool();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.put("msg", "error closing DB pool");
        }

        try {
            PostgreSQL.initPostgres(maxConnections);
            result.put("msg", "DB connection max count updated successfully to: "+ maxConnections);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            result.put("msg", "error starting DB pool");
        }

        return result;

    }
}
