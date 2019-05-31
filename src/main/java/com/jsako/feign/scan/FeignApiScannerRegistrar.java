package com.jsako.feign.scan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @Date 2019/5/27
 * @Author LLJ
 * @Description
 */
@Slf4j
public class FeignApiScannerRegistrar implements ImportBeanDefinitionRegistrar {
    private final static String LOGGER_PREFIX = ImportBeanDefinitionRegistrar.class.getSimpleName() + " registerBeanDefinitions";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        //获取FeignApiScan注解属性信息
        AnnotationAttributes feignApiScanAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(FeignApiScan.class.getName()));

        String feignManagerDef = feignApiScanAttributes.getString(FeignApiScan.FEIGN_MANAGER_DEF);
        Assert.hasLength(feignManagerDef, "FeignApiScan feignManagerDef could not be null");
        log.debug(LOGGER_PREFIX + ",feignManagerDef:{}", feignManagerDef);
        if (!isFeignManagerExists(registry, feignManagerDef)) {
            //如果FeignManger不存在,不执行扫描FeignApi
            return;
        }

        String[] basePackages = feignApiScanAttributes.getStringArray(FeignApiScan.BASE_PACKAGES);
        if (basePackages.length == 0) {
            log.error(LOGGER_PREFIX + ",FeignApiScan Annotation unset basePackages or value,please check your Configuration");
            throw new IllegalArgumentException("FeignApiScan value or basePackages is empty");
        }

        Class<? extends FeignApiFactoryBean> targetFactoryBeanClass = feignApiScanAttributes.getClass(FeignApiScan.TARGET_FACTORY_BEANCLASS);
        Assert.notNull(targetFactoryBeanClass, "FeignApiScan targetFactoryBeanClass could not be null");
        log.debug(LOGGER_PREFIX + ",targetFactoryBeanClass:{}", targetFactoryBeanClass.getName());

        LoggerEnum loggerType = feignApiScanAttributes.getEnum(FeignApiScan.LOGGER_TYPE);
        log.debug(LOGGER_PREFIX + ",loggerType:{}", loggerType);


        ClassPathFeignApiScanner scanner = new ClassPathFeignApiScanner(registry);
        scanner.setTargetFactoryBeanClass(targetFactoryBeanClass);
        scanner.setFeignManagerBeanName(feignManagerDef);
        scanner.setLoggerType(loggerType);
        log.debug(LOGGER_PREFIX + ",scanner FeignApi size:{}", Optional.ofNullable(scanner.doScan(basePackages)).map(Set::size).orElse(0));
    }

    private boolean isFeignManagerExists(BeanDefinitionRegistry registry, String feignManagerDef) {
        try {
            return Objects.nonNull(registry.getBeanDefinition(feignManagerDef));
        } catch (NoSuchBeanDefinitionException e) {
            log.error(LOGGER_PREFIX + " isFeignManagerExists", e);
            return false;
        }
    }
}
