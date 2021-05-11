package core;


import org.json.JSONObject;

public abstract class CommandDP {
    public JSONObject data;
    public Object dal;
    public abstract JSONObject execute();
    public abstract void setData(JSONObject data, Object dal);
}
