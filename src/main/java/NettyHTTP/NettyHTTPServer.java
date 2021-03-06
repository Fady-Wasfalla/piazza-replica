package NettyHTTP;

import RabbitMQ.MessageQueue;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyHTTPServer {

    public static void start(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HTTPServerInitializer());
            Channel ch = b.bind(port).sync().channel();
            MessageQueue.instantiateChannel();
            System.out.println("Server is listening on http://127.0.0.1:" + port + '/');
            ch.closeFuture().sync();
            //Initialize RabbitMq Connections
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        NettyHTTPServer.start(8080);
    }
}