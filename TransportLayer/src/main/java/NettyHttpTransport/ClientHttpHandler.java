package NettyHttpTransport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public class ClientHttpHandler extends ChannelInboundHandlerAdapter {
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //通过编解码器把byteBuf解析成FullHttpResponse
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse httpResponse = (FullHttpResponse) msg;
            HttpResponseStatus status = httpResponse.status();
            ByteBuf content = httpResponse.content();
            log.info("客户端接收响应信息：");
            log.info("status:{},content:{}", status, content.toString(CharsetUtil.UTF_8));
            httpResponse.release();
        }
    }
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        //封装请求信息
        URI uri = new URI("/test");
        String msg = "Hello";
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, uri.toASCIIString(), Unpooled.wrappedBuffer(msg.getBytes(CharsetUtil.UTF_8)));

        //构建http请求
        request.headers().set(HttpHeaderNames.HOST, "127.0.0.1");
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());

        // 发送http请求
        ctx.writeAndFlush(request);
    }
}
