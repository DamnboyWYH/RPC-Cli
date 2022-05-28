package Serializer;

import Common.Body;
import customProtocol.RPCMessage;

public interface Serializer {
    byte[] Serialize(Body mes);
    <T> T DeSerialize(byte[] bytes,Class<T> clazz);
}
