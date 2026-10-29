package com.example.awsdeveloperlabs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
class AwsDeveloperLabsApplicationTests {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }
}
