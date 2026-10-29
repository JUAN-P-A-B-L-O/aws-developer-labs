package com.example.awsdeveloperlabs.aws;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class RdsMessageService {

    private final JdbcTemplate jdbcTemplate;

    public RdsMessageService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RdsMessage> getMessages() {
        return jdbcTemplate.query("SELECT id, message FROM lab_test ORDER BY id",
                (row, rowNumber) -> new RdsMessage(row.getInt("id"), row.getString("message")));
    }

    public void createMessage(String message) {
        jdbcTemplate.update("INSERT INTO lab_test (message) VALUES (?)", message);
    }
}
