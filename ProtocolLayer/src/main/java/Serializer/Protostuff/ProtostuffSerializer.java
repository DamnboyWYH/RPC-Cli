package Serializer.Protostuff;

import Common.Body;
import Serializer.Serializer;
import customProtocol.RPCMessage;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffSerializer<T> implements Serializer {
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    @Override
    public byte[] Serialize(Body mes) {
        Class<?> clazz = mes.getClass();
        Schema schema = RuntimeSchema.getSchema(clazz);//这个是包含了序列化对象所有信息的类，包括类信息，字段信息等
        byte[] bytes;
        try {
            bytes = ProtostuffIOUtil.toByteArray(mes, schema, BUFFER);
        } finally {
            BUFFER.clear();
        }
        return bytes;
    }

    @Override
    public <T> T DeSerialize(byte[] bytes,Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }
}
