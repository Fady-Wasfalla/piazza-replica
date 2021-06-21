package core.commands.QuestionCommands;

import Services.Collections;
import Services.mongoDB;
import com.mongodb.client.model.Sorts;
import core.CommandDP;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.ArrayList;

public class AnswerQuestionCommand extends CommandDP {

    @Override
    public JSONObject execute() {
        JSONObject result = new JSONObject();
        String[] schema = {
                "questionId",
                "userName",
                "description",
                "endorsed",
                "media",
                "sort",
                "skip",
                "limit"
        };

        if (!validateJSON(schema, data)) {
            result.put("error", "invalid request parameters");
            return result;
        }
        String questionId = this.data.getString("questionId");
        int skip = this.data.getInt("skip");
        int limit = this.data.getInt("limit");
        String sort = this.data.getString("sort");

        if (sort == null) {
            sort = "title";
        }

        ArrayList<Document> myQuestions = mongoDB.readAll(mongoClient, Collections.question,
                new Document("_id", new ObjectId(questionId)), Sorts.ascending(sort), skip, limit, jedis);

        Document myQuestion;
        if (!(myQuestions.size() == 0)) {
            myQuestion = (Document) myQuestions.get(0);
        } else {
            result.put("error", "no Questions with such ID");
            return result;
        }
        System.out.println(myQuestion);
        JSONObject answers = (JSONObject) myQuestion.get("answers");

//        Object [] newAnswersArray = new Object [answers.length+1];
//        ArrayList<Object> myAnswers = new ArrayList<Object>();
//        for(int i=0; i<answers.length;i++){
//            myAnswers.add(answers[i]);
//            newAnswersArray[i]=answers[i];
//        }
        JSONObject newAnswer = new JSONObject();
        newAnswer.put("username", data.getString("userName"));
        newAnswer.put("description", data.getString("description"));
        newAnswer.put("endorsed", data.get("endorsed"));
        newAnswer.put("media", data.get("media"));

//        myAnswers.add((Object)newAnswer);
//        newAnswersArray[newAnswersArray.length-1]=newAnswer;

//        UpdateResult resultDocument = mongoDB.update(mongoClient, Collections.question,
//                new Document("questionId", questionId) ,set("answers", myAnswers), new UpdateOptions());
//
//        long modifiedQuestionsCount= resultDocument.getModifiedCount();
//        result.put("Modified Questions Count", modifiedQuestionsCount);
        return result;
    }
}
