package NettyHTTP;

import RabbitMQ.Producer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class NettyServerHandler  extends SimpleChannelInboundHandler<Object> {

    private HttpRequest request;
    private  int counter = 0;
    private String requestBody;
    private String httpRoute;
    private long correlationId;
    volatile String responseBody;
    String[] queueNames = {
            "chatRequestQueue" ,"chatResponseQueue" ,
            "courseRequestQueue" ,"courseResponseQueue" ,
            "mediaRequestQueue" ,"mediaResponseQueue" ,
            "notificationRequestQueue" ,"notificationResponseQueue" ,
            "pollRequestQueue" ,"pollResponseQueue" ,
            "questionRequestQueue" ,"questionResponseQueue" ,
            "userRequestQueue" ,"userResponseQueue"
    };


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
            writeResponse(trailer, ctx);
        }

    }

    private synchronized void writeResponse(HttpObject currentObj, final ChannelHandlerContext ctx) throws Exception{
        JSONObject requestJson = new JSONObject(getRequestBody());
        requestJson.put("httpRoute",httpRoute);
        String queueName = requestJson.getString("queue");
        if(validateQueueName(queueName)) {
            sendMessageToActiveMQ(getRequestBody(), queueName);
            System.out.println("Queue : " + queueName);
            System.out.println("Request Body : " + requestJson.toString() );
        }
    }

    private void sendMessageToActiveMQ(String jsonBody, String queue) throws IOException, TimeoutException {
        Producer P = new Producer(queue);
        P.send(jsonBody);
    }

    public boolean validateQueueName(String queue){
        return Arrays.asList(this.queueNames).contains(queue);
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
        ctx.close();
    }

}
