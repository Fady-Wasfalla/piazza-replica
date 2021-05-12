package core.commands.QuestionCommands;
import Services.mongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import core.CommandDP;
import org.bson.Document;

import java.util.ArrayList;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

public class ViewAllQuestionsCommand extends CommandDP {

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        System.out.println("Data"+this.data.toString()); // data coming from request
        ArrayList<Document> results = mongoDB.read(this.mongoClient, "questions", new Document());
        for (Document doc : results) {
            String s = doc.toJson().toString();
            JSONObject instance = new JSONObject(s);
            result.put("res", instance);
        }

        return result;
    }
}
