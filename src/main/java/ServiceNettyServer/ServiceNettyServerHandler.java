package ServiceNettyServer;

import RabbitMQ.Producer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import core.CommandDP;
import core.CommandsMap;
import io.github.cdimascio.dotenv.Dotenv;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ServiceNettyServerHandler extends SimpleChannelInboundHandler<Object> {


    private HttpRequest request;
    private  int counter = 0;
    private String requestBody;
    private String httpRoute;
    volatile String responseBody;
    private CommandsMap cmdMap;

    public ServiceNettyServerHandler(CommandsMap cmdMap){
        this.cmdMap=cmdMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //HTTP HANDLER
        if (msg instanceof HttpRequest) {
            requestBody="";
            httpRoute="";
            HttpRequest request = this.request = (HttpRequest) msg;
            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }
            httpRoute=request.uri();
        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content();
            requestBody = requestBody + content.toString(CharsetUtil.UTF_8);
            ctx.fireChannelRead(content.copy());
        }
        if (msg instanceof LastHttpContent) {
            LastHttpContent trailer = (LastHttpContent) msg;
            System.out.println(getRequestBody());
            writeResponse(trailer, ctx);
        }

    }

    private synchronized void writeResponse(HttpObject currentObj, final ChannelHandlerContext ctx) throws Exception{   
        JSONObject requestJson = new JSONObject(getRequestBody());
        Dotenv dotenv = Dotenv.load();
        MongoClient mongoClient = null;
        try  {
//            mongoClient = MongoClients.create(dotenv.get("CONNECTION_STRING")+10);
        }catch(Exception error){
            System.out.println("error hhhhhhhhhhhhh :"+error);
        }

        String function = requestJson.getString("function");
        String serviceName = requestJson.getString("service");
        CommandDP command = (CommandDP) cmdMap.queryClass(serviceName + "/" + function).getDeclaredConstructor().newInstance();
        Class service = command.getClass();
        Method setData = service.getMethod("setData",JSONObject.class, MongoClient.class);
        setData.invoke(command, requestJson,mongoClient);
        Method setCmd = service.getMethod("setCmd",CommandsMap.class);
        setCmd.invoke(command, cmdMap);
//        cmdMap.getAllClasses();
        JSONObject resultCommand = command.execute();
        if(true) {
            if(ServiceNettyHTTPServer.channel==null)
                ServiceNettyHTTPServer.instantiateChannel();
            String result = "HELLO";
            ByteBuf b = Unpooled.copiedBuffer(result, CharsetUtil.UTF_8);
            FullHttpResponse response1 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(b));
            response1.headers().set("CONTENT_TYPE", "application/json");
            response1.headers().set("CONTENT_LENGTH", response1.content().readableBytes());
            ctx.write(response1);
         }
        else {
            JSONObject result_error = new JSONObject();
            result_error.put("Message","");
            ByteBuf b = Unpooled.copiedBuffer(result_error.toString(), CharsetUtil.UTF_8);
            FullHttpResponse response1 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(b));
            response1.headers().set("CONTENT_TYPE", "application/json");
            response1.headers().set("CONTENT_LENGTH", response1.content().readableBytes());
            ctx.write(response1);
        }
    }

    public static void sendMessageToActiveMQ(String jsonBody, String queue, String UUID) throws IOException, TimeoutException {
        Producer P = new Producer(queue);
        P.send(jsonBody,UUID);
    }


    public String getRequestBody() {
        return requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }


    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
//        ctx.close();
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

}
