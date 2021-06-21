package ServiceNettyServer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import core.CommandsMap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class ServiceNettyHTTPServer {

    public static ConnectionFactory factory;
    public static Connection connection;
    public static com.rabbitmq.client.Channel channel;

    public int port;
    public String service;
    public CommandsMap cmdMap;

    public ServiceNettyHTTPServer(int port, String service, CommandsMap cmdMap) {
        this.port = port;
        this.service = service;
        this.cmdMap = cmdMap;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HTTPServerInitializer(cmdMap));
//            b.option(ChannelOption.SO_KEEPALIVE, true);
            Channel ch = b.bind(port).sync().channel();

            System.err.println(service + " Service server is listening on http://127.0.0.1:" + port + '/');

            ch.closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void instantiateChannel() {
        try {
            factory = new ConnectionFactory();
            factory.setHost("localhost");
            connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
//        ServiceNettyServerHandler x = new ServiceNettyServerHandler();
    }

}
