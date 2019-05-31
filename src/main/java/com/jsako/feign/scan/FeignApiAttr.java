package com.jsako.feign.scan;

import feign.Logger;
import feign.RequestInterceptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Date 2019/5/28
 * @Author LLJ
 * @Description FeignApi注解属性实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeignApiAttr {

    private String url;

    private HeaderAttr[] headers;

    private Class<? extends RequestInterceptor>[] requestInterceptors;

    private Logger.Level logLevel;

    private boolean decode404;

    private String username;

    private String password;

    private int readTimeout;

    private int retryTimes;

}
