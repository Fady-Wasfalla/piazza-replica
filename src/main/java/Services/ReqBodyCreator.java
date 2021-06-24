package Services;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ReqBodyCreator {

    static void createReqBody(String filePathLocally, String service, String function, String filePath, String parentPackage, String queue, String className) {
        try {
            File file = new File(filePathLocally);    //creates a new file instance
            FileReader fr = new FileReader(file);   //reads the file
            BufferedReader br = new BufferedReader(fr);  //creates a buffering character input stream
            String sb = "";    //constructs a string buffer with no characters
            String line;
            while ((line = br.readLine()) != null) {
                sb += line;     //appends line to string buffer
                sb += "\n";     //line feed
            }
            fr.close();    //closes the stream and release the resources
            JSONObject commandReqJSON = new JSONObject();
            commandReqJSON.put("service", service);
            commandReqJSON.put("function", function);
            commandReqJSON.put("sourceCode", sb);
            commandReqJSON.put("filePath", filePath);
            commandReqJSON.put("parentPackage", parentPackage);
            commandReqJSON.put("queue", queue);
            commandReqJSON.put("className", className);

            System.out.println("Request Body : ");
            System.out.println(commandReqJSON.toString());   //returns a string that textually represents the object
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static void main(String[] args) {
        createReqBody("src/main/java/core/Test.java", "command", "AddCommand"
                , "src/main/java/core", "QuestionCommands", "question", "ViewAllQuestionsCommand");
//        String sourceFile ="";
//        Path temp = null;
//        try {
//            temp = Files.move(Paths.get(String.valueOf(sourceFile)),Paths.get(String.valueOf(sourceFile).replace("src/main/java","target/classes").replace(".java",".class")));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if(temp != null)
//        {
//            System.out.println("File renamed and moved successfully");
//        }
//        else
//        {
//            System.out.println("Failed to move the file");
//        }
    }
}
