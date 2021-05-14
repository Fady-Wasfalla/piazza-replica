package Services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.InsertOneResult;
import core.commands.QuestionCommands.ViewAllQuestionsCommand;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Updates.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class mongoDB {

    final static String databaseName ="piazza";
    public static MongoCollection<Document> getCollection(MongoClient mongoClient,String collectionName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        return database.getCollection(collectionName);
    }

    public static InsertOneResult create(MongoClient mongoClient, Collections collectionName,
                                         Document document) {
        MongoCollection<Document> collection = getCollection(mongoClient, collectionName.name());
        return collection.insertOne(document);
    }

    public static ArrayList read(MongoClient mongoClient, Collections collectionName,Document filterDocument) {
        MongoCollection<Document> collection = getCollection(mongoClient,collectionName.name());
        return collection.find(filterDocument).into(new ArrayList<>());
    }

    public static void update(MongoClient mongoClient, Collections collectionName,
                              Document filterDocument, Bson updateOperation, UpdateOptions options) {
        MongoCollection<Document> collection = getCollection(mongoClient, collectionName.name());
        collection.updateOne(filterDocument, updateOperation, options);
    }

    public static void updateMany(MongoClient mongoClient, Collections collectionName,
                                  Document filterDocument, Bson updateOperation, UpdateOptions options) {
        MongoCollection<Document> collection = getCollection(mongoClient, collectionName.name());
        collection.updateMany(filterDocument, updateOperation, options);
    }

    public static void deleteOne(MongoClient mongoClient, Collections collectionName,
                                 Document filterDocument) {
        MongoCollection<Document> collection = getCollection(mongoClient, collectionName.name());
        collection.deleteOne(filterDocument);
    }

    public static void deleteMany(MongoClient mongoClient, Collections collectionName, Document filterDocument) {
        MongoCollection<Document> collection = getCollection(mongoClient, collectionName.name());
        collection.deleteMany(filterDocument);
    }

    public static void main(String[] args) {
        String connectionString = "mongodb+srv://admin:admin@cluster0.rcrnf.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            Random rand = new Random();
            Document student = new Document("_id", new ObjectId());
            student.append("student_id", 10002d)
                    .append("class_id", 1d)
                    .append("scores", Arrays.asList(new Document("type", "exam").append("score", rand.nextDouble() * 100),
                            new Document("type", "quiz").append("score", 89d),
                            new Document("type", "homework").append("score", rand.nextDouble() * 100),
                            new Document("type", "homework").append("score", rand.nextDouble() * 100)));
            // creates an instance of this json format
            //{ _id: ObjectId("6084b687d5433f16094a680b"),
            //  student_id: 10002,
            //  class_id: 1,
            //  scores: [
            //           { type: "exam",
            //            score: 89 },
            //           { type: "quiz",
            //             score: 76 },
            //           { type: "homework",
            //             score: 97 },
            //           { type: "homework",
            //             score: 27 }
            //                          ]
//            create(mongoClient, "grades", student);
//             returns all instances with student_id > 1002
//             ArrayList x = read(mongoClient,"grades",                 //
                //  new Document("student_id", new Document("$gte",10002)));     //

            // updates the first instance of student_id: 10001
            // update options like set ex: Bson updateOperation = set("comment", "You should learn MongoDB!");

           //update(mongoClient, , new Document("student_id",10001),
                //  set("message", "testing"), new UpdateOptions());
           // delete all instances with student_id > 1001
            // deleteMany(mongoClient, "grades",
              //   new Document("student_id", new Document("$gte",10001)));

//            ViewAllQuestionsCommand.viewQuestions("1");
        }
    }
}