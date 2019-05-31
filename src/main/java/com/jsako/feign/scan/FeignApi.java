package com.jsako.feign.scan;

import com.jsako.feign.manager.FeignManager;
import feign.Logger;
import feign.RequestInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Date 2019/5/27
 * @Author LLJ
 * @Description 定义FeignApi注解，封装创建FeignClient的一些参数
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FeignApi {

    String HEADERS = "headers";

    String REQUEST_INTERCEPTORS = "requestInterceptors";

    String URL = "url";

    String LOG_LEVEL = "logLevel";

    String DECODE_404 = "decode404";

    String USERNAME="username";

    String PASSWORD="password";

    String READ_TIMEOUT="readTimeout";

    String RETRY_TIMES="retryTimes";

    /**
     * 请求路径前缀
     * 支持${}表达式读取Environment配置
     */
    String url();

    /**
     * 请求头
     */
    Header[] headers() default {};

    /**
     * 请求拦截器
     */
    Class<? extends RequestInterceptor>[] requestInterceptors() default {};

    /**
     * FeignClient 输出日志级别
     */
    Logger.Level logLevel() default Logger.Level.NONE;

    /**
     * 是否404解码
     */
    boolean decode404() default false;

    /**
     * BasicAuth 用户名
     * 支持${}表达式读取Environment配置
     */
    String username() default "";

    /**
     * BasicAuth 密码
     * 支持${}表达式读取Environment配置
     */
    String password() default "";

    /**
     * 请求超时，默认30秒
     */
    int readTimeout() default FeignManager.DEFAULT_READ_TIMEOUT;

    /**
     * 重试次数 默认3次
     */
    int retryTimes() default FeignManager.DEFAULT_RETRY_TIMES;

}
