package core.commands.UserCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.internal.operation.UpdateOperation;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import static com.mongodb.client.model.Updates.set;

public class SetNotificationTokenCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {
                "userName",
                "token"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("createdAt", new Date().getTime() + "");

        Document filterDocument = new Document("token", data.getString("token"));
        mongoDB.deleteMany(Collections.token, filterDocument);
        
        filterDocument = new Document("userName", data.getString("userName"));

        Document token = mongoDB.readOne( Collections.token, filterDocument,"userName");

        if(token.size() == 0){

            Document tokenDocument = Document.parse(data.toString());

            BsonValue tokenId = mongoDB.create(Collections.token, tokenDocument,"userName")
                    .getInsertedId();

            result.put("tokenId", tokenId.asObjectId().getValue().toString());
            tokenDocument = null;
        }
        else{

            Bson updateOperation = set("token", data.getString("token"));
            mongoDB.update(Collections.token, filterDocument, updateOperation, new FindOneAndUpdateOptions(),"userName");
            updateOperation = set("createdAt", new Date().getTime() + "");
            mongoDB.update( Collections.token, filterDocument, updateOperation, new FindOneAndUpdateOptions(),"userName");
            result.put("tokenId", token.get("_id").toString());
            updateOperation = null;
        }
        schema = null;
        filterDocument = null;
        token = null;
        return result;
    }
}
