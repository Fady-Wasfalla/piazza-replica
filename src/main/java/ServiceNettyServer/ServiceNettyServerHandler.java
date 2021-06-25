package ServiceNettyServer;

import com.mongodb.client.MongoClient;
import core.CommandDP;
import DynamicClasses.CommandClassDP;
import core.CommandsMap;
import io.github.cdimascio.dotenv.Dotenv;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ServiceNettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    private HttpRequest request;
    private int counter = 0;
    private String requestBody;
    private String httpRoute;
    volatile String responseBody;
    private CommandsMap cmdMap;
    private String url;
    private ByteBuffer cntJava;
    private ByteBuffer cntClass;

    public ServiceNettyServerHandler(CommandsMap cmdMap) {
        this.cmdMap = cmdMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        //HTTP HANDLER
        if (msg instanceof HttpRequest) {
            requestBody = "";
            httpRoute = "";
            HttpRequest request = this.request = (HttpRequest) msg;
            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }
            httpRoute = request.uri();
        }
        cntClass = null;
        cntJava  = null;
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            url = String.valueOf(msg.getUri());
            ByteBuf content = httpContent.content();
            QueryStringDecoder decoder = new QueryStringDecoder(url);
            String type = decoder.parameters().get("type").get(0);

            if((type.toLowerCase()).equals("dynamicclasses")) {
                HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
                InterfaceHttpData data = postDecoder.getBodyHttpData("javaKey");
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload attribute = (FileUpload) data;
                    String value = attribute.getFilename();
                    cntJava = ((FileUpload) data).getByteBuf().nioBuffer();
                    System.out.println("fromField1 :" + value);
                }
                InterfaceHttpData data1 = postDecoder.getBodyHttpData("classKey");
                if (data1.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload attribute = (FileUpload) data1;
                    String value = attribute.getFilename();
                    cntClass = ((FileUpload) data1).getByteBuf().nioBuffer();
                    System.out.println("fromField1 :" + cntClass);
                }
            }
//            QueryStringDecoder decoder = new QueryStringDecoder(String.valueOf(msg.getUri()));
//            String serviceName = decoder.parameters().get("service").get(0);
//            String className = decoder.parameters().get("className").get(0);
//            String service = serviceName.substring(0, 1).toUpperCase() + serviceName.substring(1).toLowerCase() + "Commands";
//            String type1 = decoder.parameters().get("type1").get(0);
//
//            if(type1.equals("add")){
//                System.out.println("ENTER ADD");
//                //java file
//                String filePath1 = "src/main/java";
//                File root1 = new File(filePath1);
//                File sourceFile1 = new File(root1, "core/commands/" + service + "/" + className + ".java");
//                Files.write(sourceFile1.toPath(), StandardCharsets.UTF_8.decode(cntJava).toString().getBytes(StandardCharsets.UTF_8));
//                System.out.println("ADD NEW JAVA FILE");
//
//                String filePath = "target/classes";
//                File root = new File(filePath);
//                File sourceFile = new File(root, "core/commands/" + service + "/" + className + ".class");
//                FileChannel wChannel = new FileOutputStream(sourceFile, false).getChannel();
//                wChannel.write(cntClass);
//                wChannel.close();
//
//                Class<?> newClass = Class.forName("core.commands."+service+"."+className);
//                String key = serviceName+"/"+className.split("\\.java")[0];
//                cmdMap.replace(key,newClass);
//                cmdMap.getAllClasses();
//                System.out.println("ADD NEW CLASS FILE");
//            }
            ctx.fireChannelRead(content.copy());
        }
        if (msg instanceof LastHttpContent) {
            System.out.println("hello 2");
            LastHttpContent trailer = (LastHttpContent) msg;
            System.out.println(getRequestBody());
            writeResponse(trailer, ctx);
        }

    }

    private synchronized void writeResponse(HttpContent msg, final ChannelHandlerContext ctx) throws Exception {

        QueryStringDecoder decoder = new QueryStringDecoder(url);
        String type = decoder.parameters().get("type").get(0);

        if((type.toLowerCase()).equals("dynamicclasses")){ //dynamic classes
            String dynamicFunction = decoder.parameters().get("dynamicFunction").get(0);
            CommandClassDP command = (CommandClassDP) cmdMap.queryClass("DynamicClasses/"+dynamicFunction).getDeclaredConstructor().newInstance();
            Class service1 = command.getClass();
            Method setData = service1.getMethod("setData", ByteBuffer.class, ByteBuffer.class,String.class);
            setData.invoke(command, cntJava, cntClass,url);
            Method setCmd = service1.getMethod("setCmd", CommandsMap.class);
            setCmd.invoke(command, cmdMap);
            JSONObject resultCommand = command.execute();

            if (ServiceNettyHTTPServer.channel == null)
                ServiceNettyHTTPServer.instantiateChannel();
            ByteBuf b = Unpooled.copiedBuffer(resultCommand.toString(), CharsetUtil.UTF_8);
            FullHttpResponse response1 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(b));
            response1.headers().set("CONTENT_TYPE", "application/json");
            response1.headers().set("CONTENT_LENGTH", response1.content().readableBytes());
            ctx.write(response1);
        }else {// Controller
            JSONObject requestJson = new JSONObject(getRequestBody());
            Dotenv dotenv = Dotenv.load();
            MongoClient mongoClient = null;
            try {
//            mongoClient = MongoClients.create(dotenv.get("CONNECTION_STRING")+10);
            } catch (Exception error) {
                System.out.println("error hhhhhhhhhhhhh :" + error);
            }

            String function = requestJson.getString("function");
            String serviceName = requestJson.getString("service");
            CommandDP command = (CommandDP) cmdMap.queryClass(serviceName + "/" + function).getDeclaredConstructor().newInstance();
            Class service = command.getClass();
            Method setData = service.getMethod("setData", JSONObject.class, MongoClient.class);
            setData.invoke(command, requestJson, mongoClient);
            Method setCmd = service.getMethod("setCmd", CommandsMap.class);
            setCmd.invoke(command, cmdMap);
            JSONObject resultCommand = command.execute();
            if (true) {
                if (ServiceNettyHTTPServer.channel == null)
                    ServiceNettyHTTPServer.instantiateChannel();
                String result = "HELLO";
                ByteBuf b = Unpooled.copiedBuffer(result, CharsetUtil.UTF_8);
                FullHttpResponse response1 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(b));
                response1.headers().set("CONTENT_TYPE", "application/json");
                response1.headers().set("CONTENT_LENGTH", response1.content().readableBytes());
                ctx.write(response1);
            } else {
                JSONObject result_error = new JSONObject();
                result_error.put("Message", "");
                ByteBuf b = Unpooled.copiedBuffer(result_error.toString(), CharsetUtil.UTF_8);
                FullHttpResponse response1 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(b));
                response1.headers().set("CONTENT_TYPE", "application/json");
                response1.headers().set("CONTENT_LENGTH", response1.content().readableBytes());
                ctx.write(response1);
            }
        }
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
