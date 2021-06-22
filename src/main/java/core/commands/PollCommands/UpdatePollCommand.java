package core.commands.PollCommands;

import Services.Collections;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import core.CommandDP;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.*;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import Services.mongoDB;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;
import static com.mongodb.client.model.Updates.set;
public class UpdatePollCommand extends CommandDP {
    @Override
    public JSONObject execute() {

        JSONObject result = new JSONObject();

        
        String[] schema  = {"pollId"};
        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }
        String[] updateKeys = {
                "options",
                "expiryDate",
                "title"
        };
        BsonDocument updateOperation = new BsonDocument();
                String pollId= data.getString("pollId");
                String set = "{ $set: {";
        for (String key:updateKeys) {
            if (key.equals("pollId")) {
                  continue;
              }
            if(this.data.has(key)){
                if(key.equals("options")){
                     set+= "\"options\":" + this.data.getJSONArray(key).toString() +",";

                }
                else{
                    set+= "\""+key+"\": \"" + this.data.getString(key)+"\",";

                }
            }


        }
        set = set.substring(0,set.length()-1);
        set +=  "      } }" ;
        Document updatedPoll = mongoDB.update(mongoClient, Collections.poll,new Document("_id", new ObjectId(pollId))
                        , BsonDocument.parse(set),
                        new FindOneAndUpdateOptions(), jedis ,"_id");
            if (updatedPoll != null)
            {
                result.put("Status", "200 OK: Question updated successfully");
            }
            else {
                result.put("Status", "400 Error");
            }

        return result;
    }
}
