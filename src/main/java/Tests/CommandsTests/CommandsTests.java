package Tests.CommandsTests;

import Services.Redis;
import Services.mongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import core.commands.CourseCommands.CreateCourseCommand;
import core.commands.CourseCommands.DeleteCourseCommand;
import core.commands.CourseCommands.ViewCourseCommand;
import core.commands.PollCommands.CreatePollCommand;
import core.commands.PollCommands.DeletePollCommand;
import core.commands.QuestionCommands.*;
import core.commands.UserCommands.RegisterUserCommand;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import java.util.ArrayList;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommandsTests {
    static String courseId = null;
    static String questionId = null;
    static String pollId = null;

    String instructorUsername = "Abdo";
    String studentUsername = "Moaz";

    Dotenv dotenv = Dotenv.load();
    String connectionString = dotenv.get("CONNECTION_STRING") + "100" ; // no. of DP connections
            public CommandsTests(){
                mongoDB.initMongo();
                Redis.initRedis();
            }
    @Test
    @Order(1)
    public void CreateCourseCommand() {

        CreateCourseCommand test = new CreateCourseCommand();
        JSONObject body = new JSONObject();

        body.put("name","maths");
        body.put("userName",instructorUsername);
        body.put("code","Math101");

        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        courseId = result.getString("courseId");
        Assert.assertEquals(true,result.has("courseId"));


    }
    @Test
    @Order(2)
    public void ViewCourseCommand() {
        ViewCourseCommand test = new ViewCourseCommand();
        JSONObject body = new JSONObject();

        body.put("courseId",courseId);

        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        System.out.println("result =================================== "+result);
        JSONObject expectedId = new JSONObject(result.get("_id").toString())  ;
        Assert.assertEquals(courseId, expectedId.get("$oid").toString());

    }
    @Test
    @Order(2)
    public void RegisterInstructorCommand() {
        RegisterUserCommand test = new RegisterUserCommand();
        JSONObject body = new JSONObject();

        body.put("courseId",courseId);
        body.put("userName",instructorUsername);
        body.put("role","instructor");
        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        Assert.assertEquals(true,result.has("registeredId"));

    }
    @Test
    @Order(2)
    public void RegisterStudentCommand() {
        RegisterUserCommand test = new RegisterUserCommand();
        JSONObject body = new JSONObject();

        body.put("courseId",courseId);
        body.put("userName",studentUsername);
        body.put("role","student");
        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        Assert.assertEquals(true,result.has("registeredId"));
    }
    @Test
    @Order(3)
    void CreateQuestionCommand() {
        CreateQuestionCommand test = new CreateQuestionCommand();
        JSONObject body = new JSONObject();
        body.put("courseId",courseId);
        body.put("userName",studentUsername);
        body.put("title","Math Question 2");
        body.put("description","1+1=?");
        body.put("anonymous",false);
        body.put("private",false);
        body.put("media", new ArrayList<>());
        body.put("likes",new ArrayList<>());
        body.put("answers",new ArrayList<>());
        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        questionId = result.getString("questionId");
        Assert.assertEquals(true,result.has("questionId"));
    }

    @Test
    @Order(4)
    void AnswerQuestionCommand() {
        AnswerQuestionCommand test = new AnswerQuestionCommand();
        JSONObject body = new JSONObject();
        body.put("questionId",questionId);
        body.put("userName",instructorUsername);
        body.put("description","2");
        body.put("endorsed",false);
        body.put("private",false);
        body.put("media", new ArrayList<>());
        body.put("skip",0);
        body.put("limit",1);
        body.put("sort","userName");
        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        Assert.assertEquals(true,result.has("Modified Questions Count"));
        Assert.assertEquals(1,result.getInt("Modified Questions Count"));
    }

    @Test
    @Order(5)
    void ViewAllQuestionsCommand() {
        //creating another question for the last course
        CreateQuestionCommand qtest = new CreateQuestionCommand();
        JSONObject body = new JSONObject();
        body.put("courseId",courseId);
        body.put("userName",studentUsername);
        body.put("title","Math Question 1");
        body.put("description","1+1=?");
        body.put("anonymous",false);
        body.put("private",false);
        body.put("media", new ArrayList<>());
        body.put("likes",new ArrayList<>());
        body.put("answers",new ArrayList<>());
        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        qtest.setData(data);
        qtest.execute();
        //viewing all questions
        ViewAllQuestionsCommand test = new ViewAllQuestionsCommand();
        body = new JSONObject();
        body.put("courseId",courseId);
        body.put("skip",0);
        body.put("limit",2);
        body.put("sort","userName");
        data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        Assert.assertEquals(true,result.has("question"));
        Assert.assertEquals(2,result.getJSONArray("question").length());
    }

    @Test
    @Order(3)
    void CreatePollCommand() {
        CreatePollCommand test = new CreatePollCommand();
        JSONObject body = new JSONObject();
        body.put("courseId",courseId);
        body.put("userName",instructorUsername);
        body.put("title","title2");
        body.put("expiryDate","2/1/2021");
        body.put("options", new ArrayList<>());
        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        pollId = result.getString("pollId");
        Assert.assertEquals(true,result.has("pollId"));
    }
    @Test
    @Order(4)
    void DeleteCoursePollsCommand() {

        DeletePollCommand test = new DeletePollCommand();
        JSONObject body = new JSONObject();
        body.put("pollId",pollId);
        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        Assert.assertEquals("poll with id : " + pollId + " deleted successfully",result.getString("Status"));
    }

    @Test
    @Order(6)
    void DeleteQuestionCommand() {


        DeleteCourseQuestionsCommand test = new DeleteCourseQuestionsCommand();
        JSONObject body = new JSONObject();
        body.put("courseId",courseId);
        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        Assert.assertEquals(2,result.getInt("questionDeletedCount"));
    }

    @Test
    @Order(7)
    void DeleteCourseCommand() {
        DeleteCourseCommand test = new DeleteCourseCommand();
        JSONObject body = new JSONObject();
        body.put("courseId",courseId);
        JSONObject data = new JSONObject();
        data.put("body",body);
        data.put("user",new JSONObject());
        test.setData(data);
        JSONObject result =  test.execute();
        System.out.println(result);
        Assert.assertEquals(1,result.getInt("courseDeletedCount"));
    }
}