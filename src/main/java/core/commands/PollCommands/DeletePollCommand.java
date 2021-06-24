package core.commands.PollCommands;
import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
public class DeletePollCommand extends CommandDP{
    @Override
    public JSONObject execute() {
        JSONObject result= new JSONObject();

        String [] schema= {"pollId"};

        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String pollId= data.getString("pollId");

        Document deletedPoll = mongoDB.deleteOne( Collections.poll, new Document("_id", new ObjectId(pollId)),"_id");
        if (deletedPoll != null)
        {
            result.put("Status", "poll with id : " + pollId + " deleted successfully");
        }
        else {
            result.put("Status", "400 Error");

        }


        return result;

    }
}