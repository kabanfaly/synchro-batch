package com.michelin.gst.synchro.configuration;

import com.michelin.gst.synchro.BatchProperties;
import com.michelin.gst.synchro.entity.Journey;
import com.michelin.gst.synchro.reader.JsonFileListItemReader;
import com.michelin.gst.synchro.repository.JourneyRepository;
import com.michelin.gst.synchro.writer.DataWriter;
import org.slf4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
public class JourneyBatchConfiguration {

  private static final Logger LOGGER = getLogger(JourneyBatchConfiguration.class);

  @Bean("journeyStep")
  public Step journeyStep(StepBuilderFactory stepBuilderFactory, PlatformTransactionManager platformTransactionManager,
                          JsonFileListItemReader journeyJsonReader,
                          ItemWriter<Journey> writer, BatchProperties properties) {
    return stepBuilderFactory
        .get("journeyStep")
        .transactionManager(platformTransactionManager)
        .<Journey, Journey>chunk(properties.chunk)
        .reader(journeyJsonReader)
        .writer(writer)
        .build();
  }

  @Bean
  public Job journeyJob(JobBuilderFactory jobBuilderFactory, JobExecutionListenerSupport journeyJobExecutionListenerSupport, Step journeyStep) {
    return jobBuilderFactory.get("journeyJob")
        .incrementer(new RunIdIncrementer())
        .listener(journeyJobExecutionListenerSupport)
        .start(journeyStep)
        .build();
  }

  @Bean("journeyJsonReader")
  public JsonFileListItemReader journeyJsonReader(BatchProperties properties) {
    FileSystemResource resource = new FileSystemResource(properties.journeyJsonPath);
    JsonFileListItemReader reader = new JsonFileListItemReader();
    reader.setResource(resource);
    reader.setClassToBound(Journey.class.getCanonicalName());
    return reader;
  }

  @Bean
  public ItemWriter<Journey> journeyItemWriter(JourneyRepository journeyRepository) {
    return new DataWriter<>(journeyRepository);
  }

  @Bean
  public JobExecutionListenerSupport journeyJobExecutionListenerSupport() {
    return new JobExecutionListenerSupport() {
      @Override
      public void afterJob(org.springframework.batch.core.JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
          LOGGER.info("!!! JOURNEY JOB FINISHED !!! ");
        }
      }
    };
  }

}
