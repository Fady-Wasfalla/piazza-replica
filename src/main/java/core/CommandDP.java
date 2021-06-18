package core;

import com.mongodb.client.MongoClient;
import org.json.JSONObject;

public abstract class CommandDP {
    public JSONObject data;
    public JSONObject user;
    public Object dal;
    public MongoClient mongoClient;
    public abstract JSONObject execute();

    public void setData(JSONObject data, MongoClient mongoClient ){
        this.data = data.getJSONObject("body");
        this.user = data.getJSONObject("user");
        this.mongoClient=mongoClient;
    }

    public boolean validateJSON(String[] schema, JSONObject jsonData){

        for(int i =0; i < schema.length; i++) {
            String key = schema[i];
            if(!jsonData.has(key)){
                return false;
            }
        }
        
        return true;
    }
}
