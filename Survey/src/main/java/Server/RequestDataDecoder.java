package Server;

import DataTypes.RequestData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RequestDataDecoder extends ReplayingDecoder<RequestData> {
    private final Charset charset = StandardCharsets.UTF_8;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        RequestData data = new RequestData();
        int strLen = in.readInt();
        data.setRequest(in.readCharSequence(strLen, charset).toString());
        out.add(data);
    }
}
