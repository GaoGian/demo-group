package com.gian.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;

/**
 * Created by gaojian on 2019/4/26.
 */
public class TestTCPNetty {

    private static Logger logger = Logger.getLogger(TestTCPNetty.class);

    public static void main(String[] args){

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(1);
        ThreadFactory threadFactory = new DefaultThreadFactory("work thread");

        int processorNumber = Runtime.getRuntime().availableProcessors();
        EventLoopGroup workLoopGroup = new NioEventLoopGroup(processorNumber * 2, threadFactory, SelectorProvider.provider());

        serverBootstrap.group(bossLoopGroup, workLoopGroup);

        serverBootstrap.channel(NioServerSocketChannel.class);

        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                nioSocketChannel.pipeline().addLast(new ByteArrayEncoder());
                nioSocketChannel.pipeline().addLast(new TCPServerHandler());
                nioSocketChannel.pipeline().addLast(new ByteArrayDecoder());
            }
        });

        serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.bind(new InetSocketAddress("0.0.0.0", 83));

    }

    @ChannelHandler.Sharable
    static class TCPServerHandler extends ChannelInboundHandlerAdapter{

        private static AttributeKey<StringBuilder> contentKey = AttributeKey.valueOf("contentKey");

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception{
            logger.info("channelRegistered");
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception{
            logger.info("channelUnRegistered");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception{
            logger.info("channelActive");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception{
            logger.info("channelInActive");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
            logger.info("channelRead");
            ByteBuf byteBuf = (ByteBuf) msg;

            try {
                StringBuilder contextBuffer = new StringBuilder();
                while (byteBuf.isReadable()) {
                    contextBuffer.append((char) byteBuf.readByte());
                }

                StringBuilder content = ctx.attr(contentKey).get();
                if (content == null) {
                    content = new StringBuilder();
                    ctx.attr(contentKey).set(content);
                }
                content.append(contextBuffer);
            } catch (Exception e) {
                logger.error(e);
            } finally {
                byteBuf.release();
            }

        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
            logger.info("channelReadComplete");
            StringBuilder content = ctx.attr(contentKey).get();
            if(content.indexOf("over") == -1){
                return;
            }

            ctx.attr(contentKey).set(new StringBuilder());

            ByteBuf byteBuf = ctx.alloc().buffer(1024);
            byteBuf.writeBytes("response".getBytes());

            ctx.writeAndFlush(byteBuf);

            ctx.close();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception{
            logger.info("userEventTriggered");
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception{
            logger.info("channelWritabilityChanged");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
            logger.info("exceptionCaught");
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
            logger.info("handlerAdded");
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception{
            logger.info("handlerRemoved");
        }

    }

}