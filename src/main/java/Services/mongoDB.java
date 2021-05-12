package Services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Updates.*;

import java.util.*;


public class mongoDB {
    public static MongoClient createMongoClient(String connectionString){
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            return mongoClient;
        }
    }
    public static MongoCollection<Document> getCollection(MongoClient mongoClient, String databaseName,
                                                          String collectionName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        return database.getCollection(collectionName);
    }

    public static void create( MongoClient mongoClient, String databaseName, String collectionName, Document document,
                               jedis jedis, String key) {
        MongoCollection<Document> collection = getCollection( mongoClient, databaseName, collectionName);
        collection.insertOne(document);
        jedis.deleteCache(collectionName);
        jedis.setLayeredCache(collectionName + document.get(key).toString(), key ,(document.toJson()).toString());
    }

    public static Document readOne(MongoClient mongoClient, String databaseName, String collectionName,
                                 Document filterDocument, jedis jedis, String key) {
        String cached = jedis.getLayeredCache(collectionName + filterDocument.get(key).toString(), filterDocument.toString());
        if (cached != null)
            return Document.parse(cached);
        MongoCollection<Document> collection = getCollection(mongoClient, databaseName, collectionName);
        Document document = collection.find(filterDocument).first();
        if (document != null){
            jedis.setLayeredCache(collectionName + filterDocument.get(key).toString(), filterDocument.toString(), (document.toJson()).toString());

        return document;
               }
        return new Document();
    }

    public static Document update( MongoClient mongoClient, String databaseName, String collectionName,
                              Document filterDocument, Bson updateOperation, FindOneAndUpdateOptions options,
                                   jedis jedis, String key) {
        MongoCollection<Document> collection = getCollection(mongoClient, databaseName, collectionName);
        Document document =  collection.findOneAndUpdate(filterDocument, updateOperation, options);
        jedis.deleteCache(collectionName);
        jedis.deleteCache(collectionName + document.get(key).toString());
        jedis.setLayeredCache(collectionName + filterDocument.get(key).toString(), filterDocument.toString(),
                (document.toJson()).toString());
        return document;
    }

    public static void updateMany(MongoClient mongoClient , String databaseName, String collectionName,
                                  Document filterDocument, Bson updateOperation, UpdateOptions options, jedis jedis) {
        MongoCollection<Document> collection = getCollection( mongoClient, databaseName, collectionName);
        Set<String> cacheKeys = jedis.returnKeys(collectionName + "*");
        Iterator<String> cacheKeysIterator = cacheKeys.iterator();
        while(cacheKeysIterator.hasNext())
            jedis.deleteCache(cacheKeysIterator.next());
        collection.updateMany(filterDocument, updateOperation, options);
    }

    public static Document deleteOne(MongoClient mongoClient, String databaseName, String collectionName,
                                 Document filterDocument, jedis jedis, String key) {
        MongoCollection<Document> collection = getCollection( mongoClient, databaseName, collectionName);
        Document document = collection.findOneAndDelete(filterDocument);
        if(document != null) {
            jedis.deleteCache(collectionName);
            jedis.deleteCache(collectionName + document.get(key).toString());
            return document;
        }
        return  new Document();
    }

    public static void deleteMany(MongoClient mongoClient, String databaseName, String collectionName,
                                  Document filterDocument, jedis jedis) {
        MongoCollection<Document> collection = getCollection( mongoClient, databaseName, collectionName);
        Set<String> cacheKeys = jedis.returnKeys(collectionName + "*");
        Iterator<String> cacheKeysIterator = cacheKeys.iterator();
        while(cacheKeysIterator.hasNext())
            jedis.deleteCache(cacheKeysIterator.next());
        collection.deleteMany(filterDocument);

    }

    public static void main(String[] args) {
        String connectionString = "mongodb+srv://admin:admin@cluster0.rcrnf.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
        jedis jedis = new jedis("localhost", 6379, "");
        //check whether server is running or not
        try (MongoClient mongo_client = MongoClients.create(connectionString)) {
            System.out.println("Connection to server sucessfully");
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
//            create(mongo_client, "sample_training", "grades", student , jedis, "_id");
//             returns all instances with student_id > 1002
//            Document x = readOne(mongo_client, "sample_training", "grades",
//                    new Document("student_id", new Document("$gte", 10000)), jedis, "student_id");
            // updates the first instance of student_id: 10001
            // update options like set ex: Bson updateOperation = set("comment", "You should learn MongoDB!");
//
//            Document updated_document = update(mongo_client, "sample_training", "grades", new Document("student_id", 10002),
//                    set("message", "testing"), new FindOneAndUpdateOptions());
//            System.out.println(updated_document);
            // delete all instances with student_id > 1001
//            Document deleted_document = deleteOne(mongo_client, "sample_training", "grades",
//                    new Document("student_id", new Document("$gte", 10000)), jedis, "student_id");
//            System.out.println(deleted_document);
//            deleteMany(mongo_client, "sample_training", "grades",
//                    new Document("student_id", new Document("$gte", 10000)), jedis);
        }
    }
}