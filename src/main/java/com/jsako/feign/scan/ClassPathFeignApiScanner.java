package com.jsako.feign.scan;

import feign.Logger;
import feign.RequestInterceptor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;

import java.util.*;

/**
 * @Date 2019/5/27
 * @Author LLJ
 * @Description
 */
@Slf4j
@Getter
@Setter
public class ClassPathFeignApiScanner extends ClassPathBeanDefinitionScanner {

    private final static Class<FeignApi> DEFAULT_SCAN_ANNOTATION = FeignApi.class;

    private Class<? extends FeignApiFactoryBean> targetFactoryBeanClass;

    private String feignManagerBeanName;

    private LoggerEnum loggerType;


    public ClassPathFeignApiScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        addIncludeFilter(new AnnotationTypeFilter(DEFAULT_SCAN_ANNOTATION));
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            log.warn("No FeignApi was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            processBeanDefinitions(beanDefinitions);
        }
        return beanDefinitions;
    }

    /**
     * 将BeanDefinition封装成FeignApiFactoryBean
     *
     * @param beanDefinitions 扫描的beanDefintions
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        beanDefinitions.forEach(beanDefinitionHolder -> {

            BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
            ScannedGenericBeanDefinition scannerBeanDef = null;
            Assert.isInstanceOf(ScannedGenericBeanDefinition.class, beanDefinition, "BeanDefinition " + beanDefinition + "is not instance of ScannedGenericBeanDefinition");
            scannerBeanDef = (ScannedGenericBeanDefinition) beanDefinition;

            AnnotationMetadata metadata = scannerBeanDef.getMetadata();
            AnnotationAttributes feignApiAttr = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(DEFAULT_SCAN_ANNOTATION.getName()));
            FeignApiAttr feignApi = parseFeignApi(feignApiAttr);
            //重新设置BeanDefinition对象生成类型
            String targetProxyInterface = scannerBeanDef.getBeanClassName();

            scannerBeanDef.getConstructorArgumentValues().addGenericArgumentValue(targetProxyInterface);

            scannerBeanDef.setBeanClass(targetFactoryBeanClass);

            scannerBeanDef.getPropertyValues().addPropertyValue("loggerType",loggerType);

            scannerBeanDef.getPropertyValues().addPropertyValue("feignApiAttr", feignApi);

            scannerBeanDef.getPropertyValues().addPropertyValue("feignManager", new RuntimeBeanReference(feignManagerBeanName));
        });
    }

    /**
     * 解析FeignApi注解信息,封装成FeignApiAttr返回
     *
     * @param feignApiAttr FeignApi注解信息
     * @return
     */
    private FeignApiAttr parseFeignApi(AnnotationAttributes feignApiAttr) {
        String url = feignApiAttr.getString(FeignApi.URL);
        //校验url
        Assert.isTrue(StringUtils.isNotBlank(url),"ClassPathFeignApiScanner parseFeignApi url could not be null");
        HeaderAttr[] headers = parseHeaders(feignApiAttr.getAnnotationArray(FeignApi.HEADERS));
        Class<? extends RequestInterceptor>[] requestInterceptors = (Class<? extends RequestInterceptor>[]) feignApiAttr.getClassArray(FeignApi.REQUEST_INTERCEPTORS);
        boolean decode404 = feignApiAttr.getBoolean(FeignApi.DECODE_404);
        Logger.Level logLevel = feignApiAttr.getEnum(FeignApi.LOG_LEVEL);
        Integer readTimeout = feignApiAttr.getNumber(FeignApi.READ_TIMEOUT);
        Integer retryTimes = feignApiAttr.getNumber(FeignApi.RETRY_TIMES);
        String username = Optional.of(feignApiAttr.getString(FeignApi.USERNAME)).filter(StringUtils::isNotBlank).orElse(null);
        String password = Optional.of(feignApiAttr.getString(FeignApi.PASSWORD)).filter(StringUtils::isNotBlank).orElse(null);
        return new FeignApiAttr(url, headers, requestInterceptors, logLevel, decode404, username, password, readTimeout, retryTimes);
    }

    /**
     * 解析FeignApi注解中的headers字段信息
     *
     * @param headersAttrs headers字段信息
     * @return
     */
    private HeaderAttr[] parseHeaders(AnnotationAttributes[] headersAttrs) {
        List<HeaderAttr> headers = new ArrayList<>(headersAttrs.length);
        for (AnnotationAttributes annotationAttributes : headersAttrs) {
            headers.add(parseHeader(annotationAttributes));
        }
        HeaderAttr[] attrs = new HeaderAttr[headers.size()];
        return headers.toArray(attrs);
    }

    /**
     * 解析Header注解
     *
     * @param headersAttr AnnotationAttributes
     * @return HeaderAttr
     */
    private HeaderAttr parseHeader(AnnotationAttributes headersAttr) {
        String key = headersAttr.getString(Header.KEY);
        Assert.hasLength("key", "ClassPathFeignApiScanner parseHeader key could not be null");
        String value = headersAttr.getString(Header.VALUE);
        Assert.hasLength("value", "ClassPathFeignApiScanner parseHeader value could not be null");
        return new HeaderAttr(key, value);
    }


    /**
     * 如果是接口且独立的
     *
     * @return 是否被扫描 true 是
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }
}
