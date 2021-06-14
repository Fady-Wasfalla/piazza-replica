package NettyHTTP;

import RabbitMQ.Producer;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.cdimascio.dotenv.Dotenv;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.json.HTTP;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
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
    private HttpHeaders headers;
    volatile String responseBody;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //HTTP HANDLER
        if (msg instanceof HttpRequest) {
            requestBody="";
            httpRoute="";
            headers = ((HttpRequest) msg).headers();
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
        String queue = requestJson.getString("queue");
        String requestQueue = queue + "Req";
        String responseQueue = queue + "Res";

        JSONObject authPayload = authenticate(getToken(headers));

        if(validateQueueName(requestQueue) && authPayload != null) {

            requestJson.put("user", authPayload);

            final String corrId = UUID.randomUUID().toString();
            sendMessageToActiveMQ(requestJson.toString(), requestQueue,corrId);
            System.out.println("Request Body : " + requestJson.toString());

            final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

            if(NettyHTTPServer.channel==null)
                NettyHTTPServer.instantiateChannel();

           NettyHTTPServer.channel.basicConsume(responseQueue, false, (consumerTag, delivery) -> {

                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    NettyHTTPServer.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    response.offer(new String(delivery.getBody(), "UTF-8"));
                    NettyHTTPServer.channel.basicCancel(consumerTag);
                }else{
                    NettyHTTPServer.channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                }
            }, consumerTag -> {
            });

            String result = "";
            try {
                result = response.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ByteBuf b = Unpooled.copiedBuffer(result, CharsetUtil.UTF_8);
            FullHttpResponse response1 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(b));
            response1.headers().set("CONTENT_TYPE", "application/json");
            response1.headers().set("CONTENT_LENGTH", response1.content().readableBytes());
            ctx.write(response1);

         }
        else {
            JSONObject result_error = new JSONObject();
            result_error.put("Message","Invalid Queue Name");
            ByteBuf b = Unpooled.copiedBuffer(result_error.toString(), CharsetUtil.UTF_8);
            FullHttpResponse response1 = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(b));
            response1.headers().set("CONTENT_TYPE", "application/json");
            response1.headers().set("CONTENT_LENGTH", response1.content().readableBytes());
            ctx.write(response1);
        }
    }

    public static void sendMessageToActiveMQ(String jsonBody, String queue, String UUID) throws IOException, TimeoutException {
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
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public JSONObject authenticate(String token) {
        if(token == null)
            return null;

        Dotenv dotenv = Dotenv.load();
        String secretToken = dotenv.get("secretToken");

        try {

            Algorithm algorithm = Algorithm.HMAC256(secretToken);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            DecodedJWT decodedJwt = JWT.decode(token);

            Base64.Decoder decoder = Base64.getDecoder();
            String payload = new String(decoder.decode(decodedJwt.getPayload()));
            System.out.println(payload);
            JSONObject res = new JSONObject(payload);
            return res;
        } catch (JWTVerificationException exception){
            System.out.println("auth error");
            return null;
        }

    }

    public String getToken(HttpHeaders headers) {
        String auth = headers.get("Authorization");
        if(auth != null && auth.split(" ").length == 2 && auth.split(" ")[0].equals("Bearer"))
            return auth.split(" ")[1];
        else
            return null;
    }

}
