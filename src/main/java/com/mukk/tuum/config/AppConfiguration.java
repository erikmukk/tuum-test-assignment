package com.mukk.tuum.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;

@Configuration
@EnableSwagger2
@MapperScan(basePackages = "com.mukk.tuum.persistence.dao")
public class AppConfiguration {

    @Primary
    @Bean
    public ObjectMapper objectMapper() {
        final var df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDateFormat(df)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.mukk.tuum.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public HikariConfig dataSourceConfig() {
        return new HikariConfig();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return new HikariDataSource(dataSourceConfig());
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

}
