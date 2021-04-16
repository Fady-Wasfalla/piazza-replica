package NettyHTTP;

import RabbitMQ.Producer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class JSONHandler extends SimpleChannelInboundHandler<Object> {
    String[] queueNames = {"UserRequestQueue", "UserResponseQueue", "QuestionRequestQueue", "QuestionResponseQeue"};

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        System.out.println("JSON HANDLER");
        ByteBuf buffer = (ByteBuf) o;
        JSONObject jsonObject = new JSONObject(buffer.toString(CharsetUtil.UTF_8));
        System.out.println(jsonObject.toString());

        ByteBuf b = Unpooled.copiedBuffer(jsonObject.toString(), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(b));
        response.headers().set("CONTENT_TYPE", "application/json");
        response.headers().set("CONTENT_LENGTH", response.content().readableBytes());
        String queue = jsonObject.getString("queue");

        // forward message to correct queue
        if(validateQueueName(queue)){
            Producer P = new Producer(queue);
            P.send(jsonObject.toString());
        } else {

        }


    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public boolean validateQueueName(String queue){
        return Arrays.asList(this.queueNames).contains(queue);
    }

}
