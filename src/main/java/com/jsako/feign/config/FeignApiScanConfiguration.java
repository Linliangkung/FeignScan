package com.jsako.feign.config;

import com.jsako.feign.manager.FeignManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Date 2019/5/31
 * @Author LLJ
 * @Description
 */
@Configuration
@ConditionalOnProperty(value = "feign.scan.enabled", matchIfMissing = true)
public class FeignApiScanConfiguration {
    @Bean(name = FeignManager.DEFAULT_FEIGN_MANAGER_DEF)
    @ConditionalOnMissingBean(FeignManager.class)
    FeignManager feignManager() {
        return new FeignManager();
    }
}
