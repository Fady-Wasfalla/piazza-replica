package core.commands.QuestionCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import core.CommandDP;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CreateQuestionCommand extends CommandDP {


    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {
                "courseId",
                "userName",
                "title",
                "description",
                "anonymous",
                "private",
                "media",
                "likes",
                "answers"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("createdAt", new Date().getTime() + "");

        String description = this.data.getString("description").toLowerCase(Locale.ROOT);
        this.data.put("description",description);

        Document questionDocument = Document.parse(data.toString());

        BsonValue questionId = mongoDB.create(mongoClient, Collections.question, questionDocument)
                .getInsertedId();

        result.put("questionId", questionId.asObjectId().getValue().toString());
        
        sendNotification(questionId, data.get("courseId"));
        
        return result;
    }

    private void sendNotification(BsonValue questionId, Object courseId) {

        Document filterDocument = new Document("role", "instructor").append("course",courseId.toString());
        ArrayList<Document> instructors = mongoDB.read(mongoClient,Collections.register,filterDocument);

        for (Document d:instructors) {

            String username = d.getString("username");
            JSONObject notification = new JSONObject();
            notification.put("userName",username);
            notification.put("description","A student asked a new question");
            notification.put("model",questionId.asObjectId().getValue().toString());
            notification.put("onModel","Question");

            Document notificationDocument = Document.parse(notification.toString());

            BsonValue notificationId = mongoDB.create(mongoClient, Collections.notification, notificationDocument).getInsertedId();
        }
        

    }

}
