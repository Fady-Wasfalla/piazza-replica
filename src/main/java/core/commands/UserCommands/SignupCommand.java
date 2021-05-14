package core.commands.UserCommands;
import RabbitMQ.Producer;
import com.mongodb.client.MongoClient;
import core.CommandDP;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SignupCommand extends CommandDP {


    @Override
    public JSONObject execute() {
        System.out.println(this.data.toString());
        JSONObject result = new JSONObject();
        result.put("response",this.data.toString());
        return result;
    }


}
