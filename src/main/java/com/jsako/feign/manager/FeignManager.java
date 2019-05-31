package com.jsako.feign.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsako.feign.scan.LoggerEnum;
import feign.Feign;
import feign.Logger;
import feign.Logger.JavaLogger;
import feign.Logger.ErrorLogger;
import feign.Logger.NoOpLogger;
import feign.Request;
import feign.Retryer;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import java.util.concurrent.TimeUnit;

/**
 * Feign管理器，封装生成FeignBuilder逻辑
 */
public class FeignManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 默认读取过期时间
     */
    public static final int DEFAULT_READ_TIMEOUT = 30000;

    /**
     * 默认连接过期时间
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 10000;

    /**
     * 默认重试次数
     */
    public static final int DEFAULT_RETRY_TIMES = 3;

    /**
     * 默认FeignManager在spring容器中的BeanName
     */
    public static final String DEFAULT_FEIGN_MANAGER_DEF = "feignManager";


    /**
     * 支持BaseAuth
     *
     * @param username baseAuth 用户名
     * @param password baseAuth 密码
     * @return Feign.Builder
     */
    public Feign.Builder getFeignBuilder(String username, String password) {
        return getFeignBuilder(DEFAULT_READ_TIMEOUT, DEFAULT_RETRY_TIMES, username, password, LoggerEnum.NoOpLogger);
    }

    public Feign.Builder getFeignBuilder(int readTimeout) {
        return getFeignBuilder(readTimeout, DEFAULT_RETRY_TIMES, null, null, LoggerEnum.NoOpLogger);
    }

    public Feign.Builder getFeignBuilder() {
        return getFeignBuilder(DEFAULT_READ_TIMEOUT, DEFAULT_RETRY_TIMES, null, null, LoggerEnum.NoOpLogger);
    }

    public Feign.Builder getFeignBuilder(int readTimeout, int retryTimes, String username, String password, LoggerEnum loggerType) {

        Request.Options options = new Request.Options(DEFAULT_CONNECTION_TIMEOUT, readTimeout);
        Retryer.Default retries = new Retryer.Default(100L, TimeUnit.SECONDS.toMillis(10L), retryTimes);

        Feign.Builder builder = Feign.builder()
                .logger(buildLogger(loggerType))
                .options(options)
                .retryer(retries)
                .decoder(new JacksonDecoder(objectMapper))
                .encoder(new JacksonEncoder(objectMapper));

        if (null != username && null != password) {
            builder.requestInterceptor(new BasicAuthRequestInterceptor(username, password));
        }

        return builder;
    }

    public Logger buildLogger(LoggerEnum loggerType) {
        switch (loggerType) {
            case JavaLogger:
                return new JavaLogger();
            case ErrorLogger:
                return new ErrorLogger();
            default:
                return new NoOpLogger();
        }
    }

}
