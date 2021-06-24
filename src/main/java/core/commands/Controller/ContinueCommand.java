package core.commands.Controller;

import RabbitMQ.ConsumerMQ;
import core.CommandDP;
import org.json.JSONObject;

public class ContinueCommand extends CommandDP {
    @Override
    public JSONObject execute() {

        ConsumerMQ.isPaused = false;
        while(!ConsumerMQ.pendingTasks.isEmpty()){
            Runnable task = ConsumerMQ.pendingTasks.remove(0);
            ConsumerMQ.threadPool.submit(task);
            System.out.println("task added to threadpool; pending tasks size: " + ConsumerMQ.pendingTasks.size());
        }

        JSONObject result = new JSONObject();
        result.put("msg", "microservice continuing");
        return result;
    }
}
