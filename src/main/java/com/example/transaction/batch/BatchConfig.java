package com.example.transaction.batch;

import com.example.transaction.model.Transaction;
import com.example.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.BufferedReaderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.Resource;
import org.springframework.validation.BindException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalTime;


@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final TransactionRepository transactionRepository;
    private final TransactionProcessor transactionProcessor;

    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> reader(@Value("#{jobParameters['inputFile']}") String inputFile) {
        FlatFileItemReader<Transaction> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(inputFile));
        itemReader.setName("transactionReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());

        // Set a custom line callback to skip empty lines
        itemReader.setBufferedReaderFactory(new BufferedReaderFactory() {
            @Override
            public BufferedReader create(Resource resource, String encoding) throws UnsupportedEncodingException, IOException {
                return new BufferedReader(new InputStreamReader(resource.getInputStream(), encoding)) {
                    @Override
                    public String readLine() throws IOException {
                        String line;
                        while ((line = super.readLine()) != null) {
                            // Skip empty lines and lines with only whitespace
                            if (!line.trim().isEmpty()) {
                                return line;
                            }
                        }
                        return null;
                    }
                };
            }
        });

        return itemReader;
    }

    private LineMapper<Transaction> lineMapper() {
        DefaultLineMapper<Transaction> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer("|");
        lineTokenizer.setNames("ACCOUNT_NUMBER", "TRX_AMOUNT", "DESCRIPTION", "TRX_DATE", "TRX_TIME", "CUSTOMER_ID");
        lineTokenizer.setStrict(false);

        CustomFieldSetMapper fieldSetMapper = new CustomFieldSetMapper();

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    public static class CustomFieldSetMapper implements FieldSetMapper<Transaction> {
        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

        @Override
        public Transaction mapFieldSet(FieldSet fieldSet) throws BindException {
            Transaction transaction = new Transaction();

            transaction.setAccountNumber(fieldSet.readString("ACCOUNT_NUMBER"));
            transaction.setTrxAmount(fieldSet.readBigDecimal("TRX_AMOUNT"));
            transaction.setDescription(fieldSet.readString("DESCRIPTION"));

            String dateString = fieldSet.readString("TRX_DATE");
            String timeString = fieldSet.readString("TRX_TIME");

            transaction.setTrxDate(LocalDate.parse(dateString, DATE_FORMAT));
            transaction.setTrxTime(LocalTime.parse(timeString, TIME_FORMAT));

            transaction.setCustomerId(fieldSet.readString("CUSTOMER_ID"));

            return transaction;
        }
    }

    @Bean
    public RepositoryItemWriter<Transaction> writer() {
        RepositoryItemWriter<Transaction> writer = new RepositoryItemWriter<>();
        writer.setRepository(transactionRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step step1(FlatFileItemReader<Transaction> reader) {
        return stepBuilderFactory.get("csv-step")
                .<Transaction, Transaction>chunk(10)
                .reader(reader)
                .processor(transactionProcessor)
                .writer(writer())
                .build();
    }

    @Bean
    public Job importTransactionsJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importTransactionsJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }
}