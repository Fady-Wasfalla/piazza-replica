package core;

import com.mongodb.client.MongoClient;

import org.json.JSONObject;

public abstract class CommandDP {
    public JSONObject data;
    public Object dal;
    public MongoClient mongoClient;
    public abstract JSONObject execute();
    public void setData(JSONObject data, MongoClient mongoClient ){
        this.data = data;
        this.mongoClient=mongoClient;
    }
}
