package com.jsako.feign.scan;

import com.jsako.feign.manager.FeignManager;
import feign.Feign;
import feign.RequestInterceptor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Optional;

/**
 * @Date 2019/5/27
 * @Author LLJ
 * @Description 基于FeignApi
 */
@Getter
@Setter
@Slf4j
public class FeignApiFactoryBean<T> implements FactoryBean<T>, InitializingBean, ApplicationContextAware, EnvironmentAware {

    /**
     * FeignApi注解的一些属性，也是生成FeignClient的一些属性参数
     */
    private FeignApiAttr feignApiAttr;

    /**
     * 日志输出器类型
     */
    private LoggerEnum loggerType=LoggerEnum.NoOpLogger;

    /**
     * 被代理成FeignClient的接口类
     */
    private Class<T> targetProxyInterface;

    private FeignManager feignManager;

    private ApplicationContext applicationContext;

    private Environment environment;


    public FeignApiFactoryBean(Class<T> targetProxyInterface) {
        Assert.notNull(targetProxyInterface, "FeignApiFactoryBean targetProxyInterface could not be null");
        this.targetProxyInterface = targetProxyInterface;
    }

    @Override
    public T getObject() throws Exception {
        Feign.Builder feignBuilder = feignManager.getFeignBuilder(feignApiAttr.getReadTimeout(), feignApiAttr.getRetryTimes()
                , resolveValue(feignApiAttr.getUsername()), resolveValue(feignApiAttr.getPassword()),loggerType);
        //404解码
        if(feignApiAttr.isDecode404()){
            feignBuilder.decode404();
        }
        //日志级别设置
        feignBuilder.logLevel(feignApiAttr.getLogLevel());
        String url = resolveValue(feignApiAttr.getUrl());
        addHeaders(feignBuilder);
        addRequestInterceptors(feignBuilder);
        return feignBuilder.target(targetProxyInterface, url);
    }

    /**
     * 添加拦截器，先从spring容器中寻找对于的拦截器实例如果没有自己创建
     *
     * @param feignBuilder
     */
    private void addRequestInterceptors(Feign.Builder feignBuilder) {
        Class<? extends RequestInterceptor>[] requestInterceptors =
                feignApiAttr.getRequestInterceptors();
        if (Objects.nonNull(requestInterceptors) && requestInterceptors.length > 0) {
            for (Class<? extends RequestInterceptor> requestInterceptor : requestInterceptors) {
                RequestInterceptor findInterceptor = getBeanOfType(requestInterceptor);
                if (Objects.isNull(findInterceptor)) {
                    findInterceptor = BeanUtils.instantiateClass(requestInterceptor);
                }
                feignBuilder.requestInterceptor(findInterceptor);
            }
        }
    }

    /**
     * 添加headers
     *
     * @param feignBuilder
     */
    private void addHeaders(Feign.Builder feignBuilder) {
        HeaderAttr[] headerAttrs = feignApiAttr.getHeaders();
        if (Objects.nonNull(headerAttrs) && headerAttrs.length > 0) {
            for (HeaderAttr headerAttr : headerAttrs) {
                headerAttr.setValue(resolveValue(headerAttr.getValue()));
            }
            feignBuilder.requestInterceptor(template -> {
                for (HeaderAttr headerAttr : headerAttrs) {
                    template.header(headerAttr.getKey(), headerAttr.getValue());
                }
            });
        }
    }

    /**
     * 解析value值（${header.value}从environment中获取）
     *
     * @param value
     * @return 解析后得值
     */
    private String resolveValue(String value) {
        return Optional.ofNullable(value).filter(StringUtils::isNotBlank).map(environment::resolvePlaceholders).orElse(null);
    }

    /**
     * 根据class类型获取
     *
     * @param clazz
     * @param <M>
     * @return
     */
    private <M> M getBeanOfType(Class<M> clazz) {
        M bean = null;
        try {
            bean = applicationContext.getBean(clazz);
        } catch (BeansException e) {
            log.info("FeignApiFactoryBean getBeanOfType,class:{},do not exist in spring container,msg:{}", clazz.getName(), e.getMessage());
        }
        return bean;
    }

    @Override
    public Class<T> getObjectType() {
        return targetProxyInterface;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //校验参数
        Assert.notNull(feignApiAttr, String.format("FeignApiFactoryBean ,targetProxyInterface {%s} feignApiAttr is null,pleased check your configuration", targetProxyInterface.getSimpleName()));
        Assert.notNull(feignManager, String.format("FeignApiFactoryBean ,targetProxyInterface {%s} feignManager is null,pleased check your configuration", targetProxyInterface.getSimpleName()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
