package core.commands.QuestionCommands;

import Services.Collections;
import Services.mongoDB;
import core.CommandDP;
import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class SearchQuestionsCommand extends CommandDP {


    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();

        String [] schema = {
                "courseId",
                "query"
        };
        
        if(!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }

        String query = data.getString("query");
        query= query.toLowerCase(Locale.ROOT);
        String [] keywords = query.split(" ");

//        Document[] parameters = new Document[keywords.length];
//
//        for(int i=0; i<parameters.length; i++){
//            parameters[i] = new Document("description",
//                    new Document("$regex", ".*Ramy.*"));
//
////            System.out.println(parameters[i]);
//        }

//        Document queries = new Document("$or", parameters);
        String expression = "";
        for(int i=0; i<keywords.length; i++) {
            expression+=".*";
            expression+=keywords[i];
            expression+=".*";
            if(! (i== keywords.length-1)){
                expression+="|";
            }
            System.out.println(expression);

        }
        Document regex = new Document("$regex",expression);
        Document queryDocument = new Document("description", regex);
        
        ArrayList<Document> queryResults= mongoDB.read(mongoClient, Collections.question,
                queryDocument);

        if(queryResults.isEmpty()) {
            result.put("[]", "No questions to show for this course");
            return result;
        }
        
        for (Document doc : queryResults) {
            JSONObject instance = new JSONObject(doc.toJson().toString());
            result.append("question", instance);
        }

        return result;
    }
}
