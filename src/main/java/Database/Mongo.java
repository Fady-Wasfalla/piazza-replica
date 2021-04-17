package Database;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.*;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class Mongo {
    public static void main(String[] args) {


        MongoClient mongoClient = MongoClients.create(
                "mongodb+srv://Scalable:Scalable@scalable.rjotz.mongodb.net/Piazza?retryWrites=true&w=majority");
        MongoDatabase database = mongoClient.getDatabase("Piazza");
        
        MongoCollection<Document> Courses = database.getCollection("Courses");

        //Create Object
        Document course = new Document("name", "Scalable");
        ObjectId id = Courses.insertOne(course).getInsertedId().asObjectId().getValue();

        //Retrieve Object
        Document courseScalable = Courses.find(new Document("name", "Scalable")).first();
        System.out.println(courseScalable.toJson());
    }
}
