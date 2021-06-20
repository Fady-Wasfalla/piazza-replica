package core.commands.QuestionCommands;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Sorts;
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

        String[] schema = {
                "courseId",
                "sort",
                "skip",
                "limit"};
        
        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }
        
        String courseId = this.data.getString("courseId");
        int skip = this.data.getInt("skip");
        int limit = this.data.getInt("limit");
        String sort = this.data.getString("sort");

        if(sort == null){
            sort = "title";
        }

        ArrayList<Document> queryResults = mongoDB.readAll(this.mongoClient, Collections.question,
                new Document("courseId", courseId), Sorts.ascending(sort), skip, limit, jedis);

        if(queryResults.isEmpty()) {
            result.put("[]", "No questions to show for this course");
            return result;
        }

        for (Document doc : queryResults) {
            JSONObject instance = new JSONObject(doc.toJson().toString());
            result.append("question", instance);
        }

        System.out.println("======="+result);
        return result;
    }
}
