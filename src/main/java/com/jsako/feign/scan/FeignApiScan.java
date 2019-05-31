package com.jsako.feign.scan;

import com.jsako.feign.config.FeignApiScanConfiguration;
import com.jsako.feign.manager.FeignManager;
import feign.Logger;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Date 2019/5/27
 * @Author LLJ
 * @Description 定义FeignApi扫描注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({FeignApiScannerRegistrar.class, FeignApiScanConfiguration.class})
public @interface FeignApiScan {
    String BASE_PACKAGES="basePackages";


    String TARGET_FACTORY_BEANCLASS="targetFactoryBeanClass";


    String FEIGN_MANAGER_DEF="feignManagerDef";

    String LOGGER_TYPE="loggerType";


    /**
     * 扫描的包名
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * 扫描的包名
     *
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * 生成FeignClient的FactoryBean
     */
    Class<? extends FeignApiFactoryBean> targetFactoryBeanClass() default FeignApiFactoryBean.class;

    /**
     * 指定FeignManager Beanname
     */
    String feignManagerDef() default FeignManager.DEFAULT_FEIGN_MANAGER_DEF;

    LoggerEnum loggerType() default LoggerEnum.NoOpLogger;
}
