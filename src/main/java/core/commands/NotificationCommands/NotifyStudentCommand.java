package core.commands.NotificationCommands;

import Notifications.Notifications;
import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotifyStudentCommand extends CommandDP {


    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {
                "userName",
                "description",
                "model",
                "onModel" ,
                "sort",
                "skip",
                "limit"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("createdAt", new Date().getTime() + "");

        String model = this.data.getString("model");
        String onModel = this.data.getString("onModel");
        String description = this.data.getString("description").toLowerCase(Locale.ROOT);;
        this.data.put("description",description);



        String username = this.data.getString("userName");
        JSONObject notification = new JSONObject();
        notification.put("userName", username);
        notification.put("description", description);
        notification.put("model", model);
        notification.put("onModel", onModel);
        notification.put("createdAt", new Date().getTime() + "");

        Document notificationDocument = Document.parse(notification.toString());

        BsonValue notificationId = mongoDB.create(Collections.notification, notificationDocument,"_id").getInsertedId();

        Document tokenFilterDocument = new Document("userName", username);
        Document token = mongoDB.readOne( Collections.token, tokenFilterDocument,"userName");
        if (token.size() > 0) {
            Notifications notify = new Notifications();
            try {
                notify.notify(token.getString("token"), description);
//                    notify.notify("d3-GpfzqP5WOaJBfZB05yP:APA91bFOtEuWyXTvYcSZQI0eWhTu48IuncorBWpLyHXVdUoUMFt8d7lR5OudjOH2RiUjch47obFj_G4tDGTRTBmfCZhNzkNCLce_KhWJnhDD-5wglEJdThCS4Ps53KCpA_qGRjbwn0SP","A student asked a new question");

            } catch (Exception e) {

            }
            notify = null;
        }
        result.put("notificationsId", notificationId.asObjectId().getValue().toString());
        schema = null;
        model = null;
        onModel = null;
        description = null;
        username = null;
        notification = null;
        notificationDocument = null;
        notificationId = null;
        tokenFilterDocument = null;
        token = null;
        return result;
    }

}
