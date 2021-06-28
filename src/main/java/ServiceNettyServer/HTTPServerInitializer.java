package ServiceNettyServer;

import core.CommandsMap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.stream.ChunkedWriteHandler;


public class HTTPServerInitializer extends ChannelInitializer<SocketChannel> {
    public CommandsMap cmdMap;

    public HTTPServerInitializer(CommandsMap cmdMap) {
        this.cmdMap = cmdMap;
    }

    @Override
    protected void initChannel(SocketChannel arg0) {
        CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin()
                .allowedRequestHeaders("X-Requested-With", "Content-Type", "Content-Length")
                .allowedRequestMethods(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS)
                .build();
        ChannelPipeline p = arg0.pipeline();

        p.addLast(new CorsHandler(corsConfig));
        p.addLast(new HttpServerCodec());
        p.addLast(new ChunkedWriteHandler());
        p.addLast(new HttpObjectAggregator(64 * 1024));
        p.addLast(new ServiceNettyServerHandler(cmdMap));

    }
}
