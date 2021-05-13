package core;


import com.mongodb.client.MongoClient;
import org.json.JSONObject;
import javax.xml.validation.Schema;
import java.util.ArrayList;
import java.util.Iterator;


public abstract class CommandDP {
    public JSONObject data;
    public Object dal;
    public MongoClient mongoClient;
    public abstract JSONObject execute();
    public void setData(JSONObject data, MongoClient mongoClient ){
        this.data = data;
        this.mongoClient=mongoClient;
    }



    public boolean validateJSON(ArrayList<String> schema, JSONObject jsonData){

        for(int i =0; i < schema.size(); i++) {
            String key = schema.get(i);
            if(!jsonData.has(key)){
                return false;
            }
        }
        
        return true;
    }
}
