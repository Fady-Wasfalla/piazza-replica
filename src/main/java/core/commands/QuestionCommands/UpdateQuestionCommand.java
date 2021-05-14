package core.commands.QuestionCommands;

import Services.mongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.UpdateOptions;
import core.CommandDP;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

public class UpdateQuestionCommand extends CommandDP {
    @Override
    public JSONObject execute() {
        Dotenv dotenv = Dotenv.load();
        String connectionString = dotenv.get("CONNECTION_STRING");
        JSONObject result = new JSONObject();
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
//            mongoDB.update(MongoClient mongoClient, "questions",
//                    Document filterDocument, Bson updateOperation, UpdateOptions options);
        }
        result.put("Status", "200 OK: Question updated successfully");
        return result;
    }

}
