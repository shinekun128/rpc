package cn.ponyzhang.common;

import com.alibaba.fastjson.JSON;

import java.nio.charset.StandardCharsets;

public class Serializer {
    public <T> byte[] serialize(T obj){
        return JSON.toJSONString(obj).getBytes(StandardCharsets.UTF_8);
    }

    public <T> Object deserialize(byte[] bytes,Class<T> clazz){
        return JSON.parseObject(new String(bytes),clazz);
    }
}
