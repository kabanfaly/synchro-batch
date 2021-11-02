package com.michelin.gst.synchro.configuration;

import com.michelin.gst.synchro.BatchProperties;
import com.michelin.gst.synchro.entity.Vehicle;
import com.michelin.gst.synchro.reader.JsonFileListItemReader;
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
import com.michelin.gst.synchro.dao.VehicleDAO;

@Configuration
public class VehicleBatchConfiguration {

  private static final Logger LOGGER = getLogger(VehicleBatchConfiguration.class);

  @Bean
  public Job vehicleJob(JobBuilderFactory jobBuilderFactory, JobExecutionListenerSupport vehicleJobExecutionListenerSupport, Step vehicleStep) {
    return jobBuilderFactory.get("vehicleJob")
        .incrementer(new RunIdIncrementer())
        .listener(vehicleJobExecutionListenerSupport)
        .start(vehicleStep)
        .build();
  }

  @Bean("vehicleStep")
  public Step vehicleStep(StepBuilderFactory stepBuilderFactory, PlatformTransactionManager platformTransactionManager,
                          JsonFileListItemReader vehicleJsonReader,
                          ItemWriter<Vehicle> writer, BatchProperties properties) {
    return stepBuilderFactory
        .get("vehicleStep")
        .transactionManager(platformTransactionManager)
        .<Vehicle, Vehicle>chunk(properties.chunk)
        .reader(vehicleJsonReader)
        .writer(writer)
        .build();
  }

  @Bean("vehicleJsonReader")
  public JsonFileListItemReader vehicleJsonReader(BatchProperties properties) {
    FileSystemResource resource = new FileSystemResource(properties.vehicleJsonPath);
    JsonFileListItemReader reader = new JsonFileListItemReader();
    reader.setResource(resource);
    reader.setClassToBound(Vehicle.class.getCanonicalName());
    return reader;
  }

  @Bean
  public ItemWriter<Vehicle> vehicleItemWriter(VehicleDAO vehicleDAO) {
    return new DataWriter<>(vehicleDAO);
  }

  @Bean
  public JobExecutionListenerSupport vehicleJobExecutionListenerSupport() {
    return new JobExecutionListenerSupport() {
      @Override
      public void afterJob(org.springframework.batch.core.JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
          LOGGER.info("!!! VEHICLE JOB FINISHED !!! ");
        }
      }
    };
  }
}
