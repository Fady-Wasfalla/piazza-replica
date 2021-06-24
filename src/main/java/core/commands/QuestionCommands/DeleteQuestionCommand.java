package core.commands.QuestionCommands;

import core.CommandDP;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

public class DeleteQuestionCommand extends CommandDP {

    @Override
    public JSONObject execute() {
        Dotenv dotenv = Dotenv.load();
        String connectionString = dotenv.get("CONNECTION_STRING");
        JSONObject result = new JSONObject();
//        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
//            mongoDB.deleteOne(mongoClient, Collections.question, new Document());
//        }
        result.put("Status", "200 OK: Question deleted successfully");
        return result;
    }

}
