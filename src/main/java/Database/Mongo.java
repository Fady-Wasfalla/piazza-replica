package Database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

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
