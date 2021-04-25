package NettyHTTP;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.ImmediateEventExecutor;


public class HTTPServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelGroup group = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    @Override
    protected void initChannel(SocketChannel arg0) throws Exception {
        CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
                .allowedRequestHeaders("X-Requested-With", "Content-Type","Content-Length")
                .allowedRequestMethods(HttpMethod.GET,HttpMethod.POST,HttpMethod.PUT,HttpMethod.DELETE,HttpMethod.OPTIONS)
                .build();
//        ChannelPipeline p = arg0.pipeline();
//        p.addLast(new ChunkedWriteHandler());
//        p.addLast(new HttpObjectAggregator(64 * 1024));
//            p.addLast("decoder", new HttpRequestDecoder());
//            p.addLast("encoder", new HttpResponseEncoder());
//            p.addLast(new CorsHandler(corsConfig));
//            p.addLast(new HttpServerCodec());
//        p.addLast(new NettyServerHandler());
//        p.addLast(new WebSocketServerProtocolHandler("/chat"));
//        p.addLast(new TextWebSocketFrameHandler(group));
        ChannelPipeline pipeline = arg0.pipeline();
//        pipeline.addLast("decoder", new HttpRequestDecoder());
//        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast(new CorsHandler(corsConfig));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
        pipeline.addLast(new NettyServerHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler("/chat"));
        pipeline.addLast(new TextWebSocketFrameHandler(group));
    }
}
