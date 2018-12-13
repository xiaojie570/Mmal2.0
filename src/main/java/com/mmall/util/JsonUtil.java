package com.mmall.util;


import com.google.common.collect.Lists;
import com.mmall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by lenovo on 2018/12/11.
 */
@Slf4j
public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 对象的所有字段全部列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);

        // 取消默认转换 timestamps 形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS,false);

        // 忽略空 Bean 转 json 的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);

        // 设置 Date 的格式，所有的日期格式都统一为以下的样式，即：yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        // 忽略 在 json 字符串中存在，但是在 java 对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    // 对象转换为字符串
    public static<T> String obj2String(T obj) {
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error",e);
            return  null;
        }
    }

    // 返回一个格式化好的字符串
    public static<T> String obj2StringPretty(T obj) {
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error",e);
            return  null;
        }
    }

    // 将字符串转化为类
    public static <T> T String2Obj(String str,Class<T> clazz) {
        if (str.isEmpty() && clazz == null)
            return null;
        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    // 反序列化（带有泛型的）
    public static <T> T String2Obj(String str, TypeReference typeReference) {
        if(str.isEmpty() && typeReference == null)
            return null;

        try {
            return typeReference.getType().equals(String.class) ? (T)(str) : (T) objectMapper.readValue(str, typeReference);
        } catch (Exception e) {
            log.info("Parse String to Object error",e);
            return null;
        }
    }

    public static <T> T String2Obj(String str,Class<?> collectionsClass, Class<?> ... elementClass ) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionsClass,elementClass);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (Exception e) {
            log.info("Parse String to Object error",e);
            return null;
        }
    }

    public static void main(String[] args) {
        User user = new User();
        user.setId(1);
        user.setAnswer("asdajs");

        User user2 = new User();
        user2.setId(2);
        user2.setAnswer("asdajs");

        List<User> l = Lists.newArrayList();
        l.add(user);
        l.add(user2);
        String json = JsonUtil.obj2StringPretty(l);

        log.info("json:{}",json);

        List<User> ret = JsonUtil.String2Obj(json,List.class,User.class);

        System.out.println("end...");
    }

}
