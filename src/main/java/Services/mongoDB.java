package Services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


public class mongoDB {

    private final static String databaseName = "piazza";
    private final static int maxConnections = 50;
    private static MongoClient mongoClient;

//    public static MongoClient createMongoClient(String connectionString) {
//        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
//            return mongoClient;
//        }
//    }

    public static void initMongo() {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("CONNECTION_STRING") + maxConnections;
        mongoClient = MongoClients.create(url);
    }

    public static MongoCollection<Document> getCollection(Collections collectionName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        return database.getCollection(collectionName.name());
    }

    public static InsertOneResult create(Collections collectionName, Document document,
                                         jedis jedis, String key) {
        MongoCollection<Document> collection = getCollection(collectionName);
        InsertOneResult created_document = collection.insertOne(document);
        jedis.deleteCache(collectionName.name());
        jedis.setLayeredCache(collectionName.name() + document.get(key).toString(), key, (document.toJson()));
        return created_document;
    }

    public static Document readOne(Collections collectionName,
                                   Document filterDocument, jedis jedis, String key) {
        String cached = jedis.getLayeredCache(collectionName.name() + filterDocument.get(key).toString(), filterDocument.toString());
        if (cached != null)
            return Document.parse(cached);
        MongoCollection<Document> collection = getCollection(collectionName);
        Document document = collection.find(filterDocument).first();
        if (document != null) {
            jedis.setLayeredCache(collectionName.name() + filterDocument.get(key).toString(), filterDocument.toString(), (document.toJson()));
            return document;
        }
        return new Document();
    }

    public static ArrayList<Document> readAll(Collections collectionName,
                                              Document filterDocument, Bson sort, int skip, int limit, jedis jedis) {

        String cached_documents_string = (jedis.getLayeredCache(collectionName.name() + filterDocument.toString(), "" + skip + limit));
        JSONObject cached_documents_json = new JSONObject(cached_documents_string);
        if(cached_documents_string != null) {
            ArrayList<Document> cached_documents = new ArrayList<>();
            for (int i = 0; i < cached_documents_json.keySet().size(); i++) {
                cached_documents.add(Document.parse(cached_documents_json.getJSONObject("" + i).toString()));
            }
            return  cached_documents;
        }
        MongoCollection<Document> collection = getCollection(collectionName);
        ArrayList<Document> documents = collection.find(filterDocument).sort(sort)
                .skip(skip).limit(limit).into(new ArrayList<>());
        JSONObject cachedJsonDocuments = new JSONObject();
        for(int i = 0 ; i < documents.size() ; i++)
            cachedJsonDocuments.put(i + "" , documents.get(i));
        jedis.setLayeredCache(collectionName.name() + filterDocument.toString(), "" + skip + limit ,
                cachedJsonDocuments.toString());
        return documents;
    }

    public static Document update(Collections collectionName,
                                  Document filterDocument, Bson updateOperation, FindOneAndUpdateOptions options,
                                  jedis jedis, String key) {
        MongoCollection<Document> collection = getCollection(collectionName);
        Document document = collection.findOneAndUpdate(filterDocument, updateOperation, options);

        if (document != null) {
            jedis.deleteCache(collectionName.name());
            jedis.deleteCache(collectionName.name() + document.get(key).toString());
            jedis.setLayeredCache(collectionName.name() + filterDocument.get(key).toString(), filterDocument.toString(),
                    (document.toJson()));

            return document;
        }

        return null;
    }

    public static UpdateResult updateMany(Collections collectionName,
                                          Document filterDocument, Bson updateOperation, UpdateOptions options, jedis jedis) {
        MongoCollection<Document> collection = getCollection(collectionName);
        Set<String> cacheKeys = jedis.returnKeys(collectionName.name() + "*");
        Iterator<String> cacheKeysIterator = cacheKeys.iterator();
        while (cacheKeysIterator.hasNext())
            jedis.deleteCache(cacheKeysIterator.next());
        return collection.updateMany(filterDocument, updateOperation, options);
    }

    public static Document deleteOne(Collections collectionName,
                                     Document filterDocument, jedis jedis, String key) {
        MongoCollection<Document> collection = getCollection(collectionName);
        Document document = collection.findOneAndDelete(filterDocument);
        if (document != null) {
            jedis.deleteCache(collectionName.name());
            jedis.deleteCache(collectionName.name() + document.get(key).toString());
            return document;
        }
        return null;
    }

    public static DeleteResult deleteMany(Collections collectionName,
                                          Document filterDocument, jedis jedis) {
        MongoCollection<Document> collection = getCollection(collectionName);
        Set<String> cacheKeys = jedis.returnKeys(collectionName.name() + "*");
        Iterator<String> cacheKeysIterator = cacheKeys.iterator();
        while (cacheKeysIterator.hasNext())
            jedis.deleteCache(cacheKeysIterator.next());
        return collection.deleteMany(filterDocument);
    }


///////////////////////////////////////////////////

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
//        String connectionString = "mongodb+srv://admin:admin@cluster0.rcrnf.mongodb.net/myFirstDatabase?retryWrites=true&w=majority";
//        jedis jedis = new jedis(dotenv.get("redis_host", "localhost"), 6379, "");
//        //check whether server is running or not
//        try (MongoClient mongo_client = MongoClients.create(connectionString)) {
            System.out.println("Connection to server sucessfully");
            // Random rand = new Random();
            // Document student = new Document("_id", new ObjectId());
            // student.append("student_id", 10002d)
            //         .append("class_id", 1d)
            //         .append("scores", Arrays.asList(new Document("type", "exam").append("score", rand.nextDouble() * 100),
            //                 new Document("type", "quiz").append("score", 89d),
            //                 new Document("type", "homework").append("score", rand.nextDouble() * 100),
            //                 new Document("type", "homework").append("score", rand.nextDouble() * 100)));
//        }
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
//            create(mongoClient, "grades", student);
//             returns all instances with student_id > 1002
//             ArrayList x = read(mongoClient,"grades",                 //
        //  new Document("student_id", new Document("$gte",10002)));     //

        // updates the first instance of student_id: 10001
        // update options like set ex: Bson updateOperation = set("comment", "You should learn MongoDB!");

        //update(mongoClient, , new Document("student_id",10001),
        //  set("message", "testing"), new UpdateOptions());
        // delete all instances with student_id > 1001
        //    update(mongoClient,Collections.question , new Document("student_id",10001),
        //           set("message", "testing"), new UpdateOptions());
        // delete all instances with student_id > 1001
        // deleteMany(mongoClient, "grades",
        //   new Document("student_id", new Document("$gte",10001)));

//            ViewAllQuestionsCommand.viewQuestions("1");
//            read all operator
//            ArrayList<Document> x = readAll(mongo_client,"grades",new Document("student_id", new Document("$gte",1000)),
//                    new Document("scores",1).append("_id",0) , Sorts.ascending("scores"), 10, 5 ,jedis);
//            Iterator it = x.iterator();
//            System.out.println(x);
//            while(it.hasNext())
//                System.out.println(it.next());

    }
}