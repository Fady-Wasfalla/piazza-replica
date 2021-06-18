package core.commands.UserCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
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
        mongoDB.deleteMany(mongoClient, Collections.token, filterDocument);
        
        filterDocument = new Document("userName", data.getString("userName"));

        ArrayList<Document> token = mongoDB.read(mongoClient, Collections.token, filterDocument);

        if(token.size() == 0){

            Document tokenDocument = Document.parse(data.toString());

            BsonValue tokenId = mongoDB.create(mongoClient, Collections.token, tokenDocument)
                    .getInsertedId();

            result.put("tokenId", tokenId.asObjectId().getValue().toString());
            System.out.println("first");
        }
        else{

            Bson updateOperation = set("token", data.getString("token"));
            mongoDB.update(mongoClient, Collections.token, filterDocument, updateOperation, new UpdateOptions());
            updateOperation = set("createdAt", new Date().getTime() + "");
            mongoDB.update(mongoClient, Collections.token, filterDocument, updateOperation, new UpdateOptions());
            result.put("tokenId", token.get(0).get("_id").toString());
            System.out.println("second");
        }


        return result;
    }
}
