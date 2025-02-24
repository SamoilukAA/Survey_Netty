package Server;

import DataTypes.Topic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    public static ConcurrentHashMap<String, Topic> survey = new ConcurrentHashMap<>();
    public static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {

        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        new NettyServer(port).run();
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new RequestDataDecoder(),
                                    new ResponseDataEncoder(),
                                    new ProcessingHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            Thread consoleThread = new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                String param;
                while (true) {
                    String input = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(input)) {
                        f.channel().close();
                        break;
                    } else if (input.startsWith("load")) {
                        param = input.split(" ").length > 1 ? input.split(" ")[1] : null;
                        Gson gson = new Gson();
                        try (BufferedReader reader = new BufferedReader(new FileReader(param + ".json"))) {
                            Type surveyMapType = new TypeToken<ConcurrentHashMap<String, Topic>>(){}.getType();
                            survey = gson.fromJson(reader, surveyMapType);
                        } catch (IOException e) {
                            logger.error("Exception", e);
                        }
                    } else if (input.startsWith("save")) {
                        param = input.split(" ").length > 1 ? input.split(" ")[1] : null;
                        Gson gson =  new GsonBuilder().setPrettyPrinting().create();
                        try (FileWriter writer = new FileWriter(param + ".json")) {
                            gson.toJson(survey, writer);
                            System.out.println("File saved");
                        } catch (IOException e) {
                            logger.error("Exception", e);
                        }
                    }
                }
            });
            consoleThread.setDaemon(true);
            consoleThread.start();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
