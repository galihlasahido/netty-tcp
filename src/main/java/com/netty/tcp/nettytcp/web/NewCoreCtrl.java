package com.netty.tcp.nettytcp.web;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * com.netty.tcp
 * Created by galih.lasahido@gmail.com on 8/26/17.
 */
@RestController
public class NewCoreCtrl {

    private Logger log = Logger.getLogger(NewCoreCtrl.class.getName());

    @Autowired
    Environment env;

    @Autowired
    @Qualifier("getBootstrap")
    Bootstrap bootstrap;

    @CrossOrigin()
    @RequestMapping(value = "/api/core", produces = { MediaType.APPLICATION_JSON_VALUE }, method = RequestMethod.POST)
    public Mono<Map<String, String>> webiso(@RequestBody String json) throws IOException {
        Map<String, String> response = new HashMap<>();
        byte mac = (byte) -0x01;
        String enc = env.getProperty("server.core.enc");
        boolean isEncrypted = !(enc == null || enc.equals(""));

        String message = null;
        if (!isEncrypted) {
            mac = (byte) 0x00;
            message = json;
        } else {
            message = json+enc;
        }
        byte[] ciphertext = message.getBytes();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(ciphertext);
        out.write(mac);
        byte[] arr_combined = out.toByteArray();

        StringBuffer sb = new StringBuffer();
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {

                        ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) {
                                log.info("request CORE: "+json);
                                ctx.writeAndFlush(Unpooled.copiedBuffer(arr_combined));
                                log.info("send CORE message: "+json);
                            }

                            @Override
                            public void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
                                sb.append(buf.toString(CharsetUtil.UTF_8));

                                try {
                                    String message = sb.toString().substring(0, sb.length()-1);
                                    if (isEncrypted) {
                                        message = message+enc;
                                    } else {
                                        message = message.replaceAll("[^\\x20-\\x7E]", "");
                                    }
                                    if(message!=null) {
                                        response.put("message", message);
                                        log.info("message CORE receive: "+message);

                                    } else {
                                        log.info("data CORE receive: "+sb.toString());
                                    }
                                } catch (Exception e) {
                                    log.error("Exception message "+e.toString());
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                log.error(cause.getMessage());
                            }
                        });
                    }
                });

        return Mono.just(response);
    }
}
