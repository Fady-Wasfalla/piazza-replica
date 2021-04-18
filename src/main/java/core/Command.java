package core;


import org.json.JSONObject;

public abstract class Command {
    public JSONObject data;
    public Object dal;
    public abstract void execute();
    public abstract void setData(JSONObject data, Object dal);
}
