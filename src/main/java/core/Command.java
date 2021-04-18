package core;


import org.json.JSONObject;

public abstract class Command {
    public JSONObject data;
    public abstract void execute();
}
