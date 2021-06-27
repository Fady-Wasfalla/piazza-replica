package core.commands.PollCommands;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import core.CommandDP;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

public class AnswerPollCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        String[] schema  = {"pollId","answer"};
        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }
        String pollId= data.getString("pollId");
        JSONObject answer = data.getJSONObject("answer");
        String set = "{ $set: {" + "\"answers\":";
//        Document temp = mongoDB.readOne(mongoClient,Collections.poll,new Document("_id", new ObjectId(pollId))
//                ,jedis,"_id");

        Document temp = mongoDB.getCollection( Collections.poll).find(new Document("_id", new ObjectId(pollId))).first();
        JSONObject targetPoll = new JSONObject(temp);
        JSONArray prevAnswers =  targetPoll.getJSONArray("answers") ;
        JSONArray currentAnswers =  prevAnswers.put(answer);
        set+= (currentAnswers).toString() ;
        set +=  "      } }" ;
        Document updatedPoll = mongoDB.update( Collections.poll,new Document("_id", new ObjectId(pollId))
                , BsonDocument.parse(set),
                new FindOneAndUpdateOptions(),"_id");
        if (updatedPoll != null)
        {
            result.put("Status", "200 OK: Question updated successfully");
        }
        else {
            result.put("Status", "400 Error");
        }
        schema = null;
        pollId = null;
        answer = null;
        set = null;
        temp = null;
        targetPoll = null;
        prevAnswers = null;
        currentAnswers = null;
        updatedPoll = null;
        return result ;

    }
}
