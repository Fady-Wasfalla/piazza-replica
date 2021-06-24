package core;

import Services.Redis;
import org.json.JSONObject;

public abstract class CommandDP {
    public JSONObject data;
    public JSONObject user;
    public Object dal;
    public Redis jedis;

    public abstract JSONObject execute();

    public void setData(JSONObject data) {
        this.data = data.getJSONObject("body");
        if(data.has("user"))
            this.user = data.getJSONObject("user");
    }

    public boolean validateJSON(String[] schema, JSONObject jsonData) {

        for (int i = 0; i < schema.length; i++) {
            String key = schema[i];
            if (!jsonData.has(key)) {
                return false;
            }
        }

        return true;
    }
}
