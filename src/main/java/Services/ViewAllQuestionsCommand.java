package Services;
import Services.mongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

public class ViewAllQuestionsCommand extends CommandDP {

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        if(!this.data.keySet().contains("courseId") || !(this.data.get("courseId") instanceof String)) {
            result.put("error UPDATED5 HHHH", "invalid request");
            return result;
        }

        String courseId = this.data.getString("courseId");

        ArrayList<Document> queryResults = mongoDB.read(this.mongoClient,
                "questions",
                new Document("courseId", new ObjectId(courseId)));

        if(queryResults.isEmpty()) {
            result.put("courses", new ArrayList<JSONObject>());
            return result;
        }

        for (Document doc : queryResults) {
            JSONObject instance = new JSONObject(doc.toJson().toString());
            result.append("courses", instance);
        }
        return result;
    }
}
