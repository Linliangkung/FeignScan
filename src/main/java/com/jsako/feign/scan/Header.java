package com.jsako.feign.scan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Date 2019/5/27
 * @Author LLJ
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Header {

    String KEY="key";

    String VALUE="value";
    /**
     * 请求头的key
     */
    String key();

    /**
     * 请求头的value
     * 支持${}表达式读取Environment配置
     */
    String value();

}
