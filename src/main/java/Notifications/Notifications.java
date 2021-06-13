package Notifications;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.io.FileInputStream;
import java.io.IOException;

public class Notifications {
    public static void main(String[] args) throws IOException, FirebaseMessagingException {
        FileInputStream refreshToken = new FileInputStream("/home/vm/Downloads/client-7c3f9-firebase-adminsdk-ph90q-381bd6624a.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(refreshToken))
                .build();

        FirebaseApp.initializeApp(options);
        
        // This registration token comes from the client FCM SDKs.
        String registrationToken = "d3-GpfzqP5WOaJBfZB05yP:APA91bFOtEuWyXTvYcSZQI0eWhTu48IuncorBWpLyHXVdUoUMFt8d7lR5OudjOH2RiUjch47obFj_G4tDGTRTBmfCZhNzkNCLce_KhWJnhDD-5wglEJdThCS4Ps53KCpA_qGRjbwn0SP";

        // See documentation on defining a message payload.
        Message message = Message.builder()
                .putData("Course", "Math")
                .putData("Description", "You failed the course")
                .setToken(registrationToken)
                .build();

        // Send a message to the device corresponding to the provided registration token.
        String response = FirebaseMessaging.getInstance().send(message);

        // Response is a message ID string.
        System.out.println("Successfully sent message: " + response);

    }
}
