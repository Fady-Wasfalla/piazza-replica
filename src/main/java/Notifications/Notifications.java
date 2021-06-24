package Notifications;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Notifications {
    public static FileInputStream refreshToken;
    static boolean initialized = false;

    public Notifications(){
        if(!initialized){
            initialize();
            initialized = true;
        }
    }

    public void initialize(){
        try {
            refreshToken = new FileInputStream("src/main/java/Notifications/client-7c3f9-firebase-adminsdk-ph90q-381bd6624a.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(refreshToken))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String notify(String registrationToken, String description) throws IOException, FirebaseMessagingException {

        // This registration token comes from the client FCM SDKs.

        // See documentation on defining a message payload.
        Message message = Message.builder()
                .putData("Description", description)
                .setToken(registrationToken)
                .build();

        // Send a message to the device corresponding to the provided registration token.
        String response = FirebaseMessaging.getInstance().send(message);

        // Response is a message ID string.
        System.out.println("Successfully sent message: " + response);

        return response;
    }
}