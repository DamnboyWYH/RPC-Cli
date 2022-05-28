package Serializer.Json;

import Common.Body;
import Serializer.Serializer;
import com.alibaba.fastjson.JSON;
import customProtocol.RPCMessage;

import java.nio.charset.StandardCharsets;

public class JsonSerializer implements Serializer {
    @Override
    public byte[] Serialize(Body mes) {
        String JsonString = JSON.toJSONString(mes);
        return JsonString.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T DeSerialize(byte[] bytes, Class<T> clazz) {
        String JsonString = new String(bytes);
        return JSON.parseObject(JsonString,clazz);
    }
}
