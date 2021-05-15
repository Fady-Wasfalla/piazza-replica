package Services;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TRY {
    public static void main(String[] args) {
        try {
            File file = new File("src/main/java/Services/AddCommand.java");    //creates a new file instance
            FileReader fr = new FileReader(file);   //reads the file
            BufferedReader br = new BufferedReader(fr);  //creates a buffering character input stream
            String sb = "";    //constructs a string buffer with no characters
            String line;
            while ((line = br.readLine()) != null) {
                sb+=line;     //appends line to string buffer
                sb+="\n";     //line feed
            }
            fr.close();    //closes the stream and release the resources
            JSONObject commandReqJSON = new JSONObject();
            commandReqJSON.put("service","command");
            commandReqJSON.put("function","AddCommand");
            commandReqJSON.put("sourceCode",sb);
            commandReqJSON.put("filePath",sb);
            commandReqJSON.put("parentPackage",sb);
            commandReqJSON.put("className",sb);

            System.out.println("Contents of File: ");
            System.out.println(commandReqJSON.toString());   //returns a string that textually represents the object
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
