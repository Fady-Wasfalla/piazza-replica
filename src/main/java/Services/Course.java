package Services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;

public class Course {
    String name;
    String code;
    long createdAt;

    public Course(String name, String code, long createdAt){
        this.name = name;
        this.code = code;
        this.createdAt = createdAt;

    }

    public InsertOneResult create(MongoClient mongoClient){


        InsertOneResult insertOneResult = mongoDB.create(mongoClient, Collections.course, new Document());

        return insertOneResult;

    }
}
