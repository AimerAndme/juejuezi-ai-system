package com.yupi.yuaiagent.config;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Kryo 序列化工具类（处理线程安全和多态类型）
 */
public class KryoSerializer {

    // 线程局部变量：每个线程一个 Kryo 实例（解决线程安全问题）
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 关键：注册 Message 接口及其实现类（多态支持）
        // 注册顺序：先接口，再实现类
        Registration messageReg = kryo.register(Message.class);
        kryo.register(UserMessage.class, messageReg.getId() + 1); // 自定义 ID，避免冲突
        kryo.register(AssistantMessage.class, messageReg.getId() + 2);
        // 2. 关键：注册 JDK 集合类（解决当前 ArrayList 未注册问题）
        kryo.register(ArrayList.class); // 注册 ArrayList
        kryo.register(List.class); // 可选：注册 List 接口（增强兼容性）
        // 3. 关键：注册 MessageType 类（解决当前错误）
        kryo.register(MessageType.class);
        // 在 Kryo 初始化时添加
        kryo.register(HashMap.class);
        kryo.register(LinkedList.class);
        kryo.register(String.class); // 虽然 String 通常会被自动处理，但显式注册更稳妥
        // 可选：关闭默认的循环引用检测（若确认无循环引用，可提升性能）
        kryo.setReferences(false);
        return kryo;
    });

    /**
     * 序列化：对象 → 字节数组
     */
    public static byte[] serialize(Object obj) throws IOException {
        if (obj == null) {
            return null;
        }
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Output output = new Output(bos)) {
            kryo.writeObject(output, obj);
            output.flush();
            return bos.toByteArray();
        }
    }

    /**
     * 反序列化：字节数组 → 对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        if (data == null || data.length == 0) {
            return null;
        }
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             Input input = new Input(bis)) {
            return kryo.readObject(input, clazz);
        }
    }
}