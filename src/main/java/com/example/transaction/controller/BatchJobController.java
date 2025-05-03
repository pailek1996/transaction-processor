package com.example.transaction.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job importTransactionsJob;

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importTransactions(
            @RequestParam("file") String filePath) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Clean the file path
            String cleanFilePath = filePath.trim().replace("\n", "").replace("\r", "");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFile", cleanFilePath)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            // Execute job synchronously to get the actual result
            JobExecution jobExecution = jobLauncher.run(importTransactionsJob, jobParameters);

            // Get job status
            BatchStatus status = jobExecution.getStatus();

            response.put("jobId", jobExecution.getJobId());
            response.put("status", status.toString());

            if (status == BatchStatus.COMPLETED) {
                // Get job execution details
                Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
                Map<String, Object> stepStats = new HashMap<>();

                for (StepExecution stepExecution : stepExecutions) {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("readCount", stepExecution.getReadCount());
                    stats.put("writeCount", stepExecution.getWriteCount());
                    stats.put("commitCount", stepExecution.getCommitCount());
                    stats.put("filterCount", stepExecution.getFilterCount());
                    stats.put("skipCount", stepExecution.getSkipCount());
                    stepStats.put(stepExecution.getStepName(), stats);
                }

                response.put("stepExecutions", stepStats);
                response.put("message", "Batch job completed successfully");
                return ResponseEntity.ok(response);
            } else if (status == BatchStatus.FAILED) {
                // Get failure exceptions
                List<String> errors = new ArrayList<>();
                for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                    for (Throwable failureException : stepExecution.getFailureExceptions()) {
                        errors.add(failureException.getMessage());
                    }
                }

                response.put("errors", errors);
                response.put("message", "Batch job failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            } else {
                response.put("message", "Batch job status: " + status);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            log.error("Error starting batch job", e);
            response.put("status", "error");
            response.put("message", "Failed to start batch job: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}