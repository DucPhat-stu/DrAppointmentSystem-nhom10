package com.healthcare.appointment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(InternalServiceProperties.class)
public class InternalWebConfig implements WebMvcConfigurer {
    private final InternalServiceProperties internalServiceProperties;

    public InternalWebConfig(InternalServiceProperties internalServiceProperties) {
        this.internalServiceProperties = internalServiceProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InternalServiceTokenInterceptor(internalServiceProperties))
                .addPathPatterns("/internal/**");
    }
}
