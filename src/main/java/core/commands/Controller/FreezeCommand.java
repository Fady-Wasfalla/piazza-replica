package core.commands.Controller;

import RabbitMQ.ConsumerMQ;
import core.CommandDP;
import org.json.JSONObject;

public class FreezeCommand extends CommandDP {
    @Override
    public JSONObject execute() {

        ConsumerMQ.isPaused = true;
        JSONObject result = new JSONObject();
        result.put("msg", "thread pool frozen successfully");

        return result;
    }
}
