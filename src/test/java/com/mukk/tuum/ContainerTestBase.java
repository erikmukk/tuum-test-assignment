package com.mukk.tuum;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@ContextConfiguration(
        initializers = {ContainerTestBase.Initializer.class},
        classes = TestTuumApplication.class
)
@Testcontainers
@Transactional
public abstract class ContainerTestBase {

    private static final PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER;

    static {
        POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:13")
                .withDatabaseName("integration_db")
                .withUsername("db_user")
                .withPassword("it")
                .withInitScript("_dbinit/__dbcreate.sql");
        POSTGRES_SQL_CONTAINER.start();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + POSTGRES_SQL_CONTAINER.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRES_SQL_CONTAINER.getUsername(),
                    "spring.datasource.password=" + POSTGRES_SQL_CONTAINER.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
