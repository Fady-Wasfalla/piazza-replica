package core.commands.Controller;

import RabbitMQ.ConsumerMQ;
import core.CommandDP;
import org.json.JSONObject;

import java.util.concurrent.Executors;

public class SetMaxThreadCountCommand extends CommandDP {

    @Override
    public JSONObject execute() {

        String[] schema = {"threadCount"};
        JSONObject result = new JSONObject();

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        int threadCount = this.data.getInt("threadCount");

        ConsumerMQ.threadPool.shutdown();
        ConsumerMQ.threadPool = Executors.newFixedThreadPool(threadCount);

        result.put("msg", threadCount + " threads");

        return result;


    }
}
