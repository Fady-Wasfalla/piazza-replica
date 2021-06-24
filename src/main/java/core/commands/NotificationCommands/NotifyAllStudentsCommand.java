package core.commands.NotificationCommands;

import Notifications.Notifications;
import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.model.Sorts;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotifyAllStudentsCommand extends CommandDP {


    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {
                "userName",
                "courseId",
                "description",
                "model",
                "onModel",
                "sort",
                "skip",
                "limit"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("createdAt", new Date().getTime() + "");

        String courseId = this.data.getString("courseId");
        String model = this.data.getString("model");
        String onModel = this.data.getString("onModel");
        String description = this.data.getString("description").toLowerCase(Locale.ROOT);;
        String sort = this.data.getString("sort");
        int skip = this.data.getInt("skip");
        int limit = this.data.getInt("limit");
        this.data.put("description",description);
        this.data.put("description",description);

        Document filterDocument = new Document("role", "student").append("courseId",courseId);
        ArrayList<Document> students = mongoDB.readAll(mongoClient,Collections.register,filterDocument, Sorts.ascending(sort),skip,limit,jedis);

        ArrayList<String> results = new ArrayList<>();
        for (Document d:students) {

            String username = d.getString("userName");
            if(!username.equals(this.data.getString("userName"))) {
                JSONObject notification = new JSONObject();
                notification.put("userName", username);
                notification.put("description", description);
                notification.put("model", model);
                notification.put("onModel", onModel);
                notification.put("createdAt", new Date().getTime() + "");

                Document notificationDocument = Document.parse(notification.toString());

                BsonValue notificationId = mongoDB.create(mongoClient, Collections.notification, notificationDocument,jedis,"_id").getInsertedId();
                results.add(notificationId.asObjectId().getValue().toString());

                Document tokenFilterDocument = new Document("userName", username);
                Document token = mongoDB.readOne(mongoClient, Collections.token, tokenFilterDocument,jedis,"userName");
                if (token.size() > 0) {
                    Notifications notify = new Notifications();
                    try {
                        notify.notify(token.getString("token"), description);
//                    notify.notify("d3-GpfzqP5WOaJBfZB05yP:APA91bFOtEuWyXTvYcSZQI0eWhTu48IuncorBWpLyHXVdUoUMFt8d7lR5OudjOH2RiUjch47obFj_G4tDGTRTBmfCZhNzkNCLce_KhWJnhDD-5wglEJdThCS4Ps53KCpA_qGRjbwn0SP","A student asked a new question");

                    } catch (Exception e) {

                    }
                }
            }


        }
        result.put("notificationsId", results.toString());
        return result;
    }

}
