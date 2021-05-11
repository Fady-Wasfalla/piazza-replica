package NettyHTTP;

import RabbitMQ.Producer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
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
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class NettyServerHandler  extends SimpleChannelInboundHandler<Object> {


    private HttpRequest request;
    private  int counter = 0;
    private String requestBody;
    private String httpRoute;
    volatile String responseBody;

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
        String responseQueue = queueName.split("Req",0)[0]+"Res";

//        if(validateQueueName(queueName)) {
            final String corrId = UUID.randomUUID().toString();
            sendMessageToActiveMQ(requestJson.toString(), queueName,corrId);
            System.out.println("Request Body : " + requestJson.toString());

            final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

            if(NettyHTTPServer.channel==null)
                NettyHTTPServer.instantiateChannel();
            NettyHTTPServer.channel.queueDeclare(responseQueue, false, false, false, null);
            System.out.println("[*RES] "+responseQueue + " [*] Waiting for messages. To exit press CTRL+C");
            String ctag = NettyHTTPServer.channel.basicConsume(responseQueue, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    response.offer(new String(delivery.getBody(), "UTF-8"));
                }
            }, consumerTag -> {
            });

            String result = "";
            try {
                result = response.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            NettyHTTPServer.channel.basicCancel(ctag);
            ByteBuf b = Unpooled.copiedBuffer(result, CharsetUtil.UTF_8);
            FullHttpResponse response1 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(b));
            response1.headers().set("CONTENT_TYPE", "application/json");
            response1.headers().set("CONTENT_LENGTH", response1.content().readableBytes());
            ctx.write(response1);
            System.out.println(response1.toString());
//        }
    }

    private void sendMessageToActiveMQ(String jsonBody, String queue, String UUID) throws IOException, TimeoutException {
        Producer P = new Producer(queue);
        P.send(jsonBody,UUID);
    }

    public boolean validateQueueName(String queue){
        Dotenv dotenv = Dotenv.load();
        String strlist = dotenv.get("queues");
        return Arrays.asList(strlist.split(",")).contains(queue);
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
