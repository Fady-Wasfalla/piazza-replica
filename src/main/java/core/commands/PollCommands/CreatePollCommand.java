package core.commands.PollCommands;

import Notifications.Notifications;
import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.BsonValue;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class CreatePollCommand extends CommandDP {

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String[] schema = {
                "courseId",
                "userName",
                "options",
                "expiryDate",
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        this.data.put("answers", new JSONArray());
        this.data.put("createdAt", new Date().getTime() + "");

        Document pollDocument = Document.parse(data.toString());

        BsonValue pollId = mongoDB.create(mongoClient, Collections.poll, pollDocument)
                .getInsertedId();

        result.put("pollId", pollId.asObjectId().getValue().toString());

        sendNotification(pollId, data.get("courseId").toString());

        return result;
    }

    private void sendNotification(BsonValue pollId, String courseId) {

        Document filterDocument = new Document("role", "student").append("courseId",courseId).append("banned",false);
        ArrayList<Document> students = mongoDB.read(mongoClient,Collections.register,filterDocument);

        for (Document d:students) {

            String username = d.getString("userName");
            JSONObject notification = new JSONObject();
            notification.put("userName",username);
            notification.put("description","An instructor posted a new poll");
            notification.put("model",pollId.asObjectId().getValue().toString());
            notification.put("onModel","Poll");
            notification.put("createdAt", new Date().getTime() + "");

            Document notificationDocument = Document.parse(notification.toString());

            BsonValue notificationId = mongoDB.create(mongoClient, Collections.notification, notificationDocument).getInsertedId();

            Document tokenFilterDocument = new Document("userName", username);
            ArrayList<Document> token = mongoDB.read(mongoClient, Collections.token, tokenFilterDocument);
            if(token.size()>0){
                Notifications notify = new Notifications();
                try {
                    notify.notify(token.get(0).getString("token"),"An instructor posted a new poll");
//                    notify.notify("d3-GpfzqP5WOaJBfZB05yP:APA91bFOtEuWyXTvYcSZQI0eWhTu48IuncorBWpLyHXVdUoUMFt8d7lR5OudjOH2RiUjch47obFj_G4tDGTRTBmfCZhNzkNCLce_KhWJnhDD-5wglEJdThCS4Ps53KCpA_qGRjbwn0SP","A student asked a new question");

                } catch (Exception e) {

                }
            }
        }


    }


}
