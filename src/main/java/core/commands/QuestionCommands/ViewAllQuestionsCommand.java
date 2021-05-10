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
        Dotenv dotenv = Dotenv.load();
        String connectionString = dotenv.get("CONNECTION_STRING");
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            ArrayList<Document> results = mongoDB.read(mongoClient,"questions", new Document());
            for(Document doc: results) {
                System.out.println(doc.toJson().toString());
            }
            JSONObject result = new JSONObject();
            result.put("response","HELLO from questions");
            return result;
        }
    }

    @Override
    public void setData(JSONObject data, Object dal) {
        this.data = data;
        this.dal = dal;

    }
//
//    public static ArrayList<Document> viewQuestions(String courseId){
//
//
//    }
}
