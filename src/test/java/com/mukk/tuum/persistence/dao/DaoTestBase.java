package com.mukk.tuum.persistence.dao;

import com.mukk.tuum.ContainerTestBase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dao-test")
@SpringBootTest
public abstract class DaoTestBase extends ContainerTestBase {
}
