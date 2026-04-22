package com.healthcare.shared.common.config;

import com.healthcare.shared.common.web.ApiResponseFactory;
import com.healthcare.shared.common.web.RequestContextFilter;
import com.healthcare.shared.common.web.RequestMetadataContext;
import com.healthcare.shared.common.web.RequestMetadataProvider;
import com.healthcare.shared.common.web.ThreadLocalRequestMetadataContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AppServiceProperties.class)
public class CommonWebConfiguration {
    @Bean
    Clock requestClock() {
        return Clock.systemUTC();
    }

    @Bean
    RequestMetadataContext requestMetadataContext(Clock requestClock) {
        return new ThreadLocalRequestMetadataContext(requestClock);
    }

    @Bean
    ApiResponseFactory apiResponseFactory(RequestMetadataProvider requestMetadataProvider) {
        return new ApiResponseFactory(requestMetadataProvider);
    }

    @Bean(name = "requestMetadataFilter")
    RequestContextFilter requestMetadataFilter(RequestMetadataContext requestMetadataContext) {
        return new RequestContextFilter(requestMetadataContext);
    }
}
