package com.mukk.tuum.config;

import com.mukk.tuum.interceptor.DatabaseInterceptor;
import org.aspectj.lang.Aspects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseInterceptorConfig {

    @Bean
    public DatabaseInterceptor getAutowireCapableDatabaseInterceptor() {
        return Aspects.aspectOf(DatabaseInterceptor.class);
    }

}
