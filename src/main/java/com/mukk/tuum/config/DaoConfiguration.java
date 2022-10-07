package com.mukk.tuum.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.mukk.tuum.persistence.dao")
public class DaoConfiguration {
}
