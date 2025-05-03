package com.example.transaction.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");

            String query = "SELECT COUNT(*) FROM transaction";
            Integer count = jdbcTemplate.queryForObject(query, Integer.class);

            log.info("Found {} records in the database.", count);

            // Log some statistics by account and transaction type
            jdbcTemplate.query("SELECT account_number, COUNT(*) as count FROM transaction GROUP BY account_number",
                            (rs, row) -> rs.getString(1) + ": " + rs.getInt(2))
                    .forEach(result -> log.info("Transactions by account: {}", result));

            jdbcTemplate.query("SELECT description, COUNT(*) as count FROM transaction GROUP BY description",
                            (rs, row) -> rs.getString(1) + ": " + rs.getInt(2))
                    .forEach(result -> log.info("Transactions by type: {}", result));
        }
    }
}
