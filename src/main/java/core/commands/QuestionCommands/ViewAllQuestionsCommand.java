package core.commands.QuestionCommands;
import Services.mongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;

import java.util.ArrayList;
import io.github.cdimascio.dotenv.Dotenv;
public class ViewAllQuestionsCommand {

    public static ArrayList<Document> viewQuestions(String courseId){
        Dotenv dotenv = Dotenv.load();  
        String connectionString = dotenv.get("CONNECTION_STRING");
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            ArrayList<Document> results = mongoDB.read(mongoClient,"Questions", new Document());
            return results;
        }

    }
}
