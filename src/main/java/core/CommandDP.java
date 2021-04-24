package core;


import org.json.JSONObject;

public abstract class CommandDP {
    public JSONObject data;
    public Object dal;
    public abstract void execute();
    public abstract void setData(JSONObject data, Object dal);
}
