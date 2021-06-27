package DynamicClasses;

import org.json.JSONObject;

import java.nio.ByteBuffer;

public abstract class CommandClassDP {


    public ByteBuffer javaFile;
    public ByteBuffer classFile;
    public String url;

    public abstract JSONObject execute();

    public void setData(ByteBuffer javaFile, ByteBuffer classFile, String url) {
        this.javaFile=javaFile;
        this.classFile=classFile;
        this.url=url;
    }

}
