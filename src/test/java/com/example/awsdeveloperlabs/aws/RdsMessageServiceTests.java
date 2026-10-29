package com.example.awsdeveloperlabs.aws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class RdsMessageServiceTests {

    @Test
    void readsOrderedMessagesAndMapsColumns() throws Exception {
        var jdbcTemplate = mock(JdbcTemplate.class);
        var row = mock(ResultSet.class);
        when(row.getInt("id")).thenReturn(1);
        when(row.getString("message")).thenReturn("RDS funcionando");
        when(jdbcTemplate.query(eq("SELECT id, message FROM lab_test ORDER BY id"),
                org.mockito.ArgumentMatchers.<RowMapper<RdsMessage>>any())).thenAnswer(invocation -> {
                    RowMapper<RdsMessage> mapper = invocation.getArgument(1);
                    return List.of(mapper.mapRow(row, 0));
                });

        assertEquals(List.of(new RdsMessage(1, "RDS funcionando")),
                new RdsMessageService(jdbcTemplate).getMessages());
    }

    @Test
    void insertsMessageAsBoundParameter() {
        var jdbcTemplate = mock(JdbcTemplate.class);
        String message = "It's connected; SELECT 1";

        new RdsMessageService(jdbcTemplate).createMessage(message);

        verify(jdbcTemplate).update("INSERT INTO lab_test (message) VALUES (?)", message);
    }
}
