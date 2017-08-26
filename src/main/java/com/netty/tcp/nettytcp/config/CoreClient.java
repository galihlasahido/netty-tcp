package com.netty.tcp.nettytcp.config;

import com.netty.tcp.nettytcp.utils.ExpressiveConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

/**
 * om.netty.tcp
 * Created by galih.lasahido@gmail.com on 8/26/17.
 */
@Configuration
public class CoreClient {

    boolean isEncrypted;
    private EventLoopGroup group = new OioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();
    private Logger log = Logger.getLogger(CoreClient.class.getName());

    @Bean
    @Qualifier("getGroup")
    public EventLoopGroup getGroup() {
        return group;
    }

    @Bean
    @Qualifier("getBootstrap")
    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    @Autowired
    ExpressiveConfig env;

    @PreDestroy
    void stop() throws Exception {
        group.shutdownGracefully();
    }

    @PostConstruct
    void init() {
        group = new OioEventLoopGroup();
        bootstrap.group(group)
                .channel(OioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 5120)
                .option(ChannelOption.SO_SNDBUF, 5120)
                .remoteAddress(new InetSocketAddress(env.getProperty("server.core.host"), Integer.valueOf(env.getProperty("server.core.port"))))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler());
                    }
                });

        ChannelFuture f = bootstrap.connect();

        if(f.channel().isWritable())
            log.info("its isWritable");
        else
            log.info("its not isWritable");

    }

}