package core.commands.QuestionCommands;

import Services.mongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import core.CommandDP;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.json.JSONObject;

public class CreateQuestionCommand extends CommandDP {


    @Override
    public JSONObject execute() {
        Dotenv dotenv = Dotenv.load();
        String connectionString = dotenv.get("CONNECTION_STRING");
        JSONObject result = new JSONObject();
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            mongoDB.create(mongoClient,"questions", new Document());
        }
        result.put("Status", "200 OK: Question created successfully"); 
        return result;
    }

}
