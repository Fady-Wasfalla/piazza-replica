package DynamicClasses;

import core.CommandsMap;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


public class DeleteCommand extends CommandClassDP {

    public CommandsMap cmdMap;

    public void setCmd(CommandsMap cmdMap) {
        this.cmdMap = cmdMap;
    }

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        Boolean status = true;
        QueryStringDecoder decoder = new QueryStringDecoder(String.valueOf(this.url));
        String serviceName = decoder.parameters().get("service").get(0);
        String className = decoder.parameters().get("className").get(0);
        String key = serviceName+"/"+className.split("\\.java")[0];
        cmdMap.remove(key);
        System.out.println("Class Deleted Successfully");
        if( status ) {
            result.append("Message","Class Deleted Successfully");
        }
        return result;
    }
}
