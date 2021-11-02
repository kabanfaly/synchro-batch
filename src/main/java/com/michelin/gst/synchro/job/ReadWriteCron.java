package com.michelin.gst.synchro.job;

import com.michelin.gst.synchro.configuration.BatchConfiguration;
import org.slf4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.boot.autoconfigure.batch.BasicBatchConfigurer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ReadWriteCron {

  private static final Logger LOGGER = getLogger(BatchConfiguration.class);

  private final BasicBatchConfigurer basicBatchConfigurer;

  private final Job vehicleJob;

  private final Job journeyJob;

  public ReadWriteCron(BasicBatchConfigurer basicBatchConfigurer, Job vehicleJob, Job journeyJob) {
    this.basicBatchConfigurer = basicBatchConfigurer;
    this.vehicleJob = vehicleJob;
    this.journeyJob = journeyJob;
  }

  @Scheduled(cron = "${batch.read-write.cron}")
  public void runReadWrite() throws Exception {

    LOGGER.info("Job Started at :" + new Date());

    JobParameters param = new JobParametersBuilder().addString("JobID",
        String.valueOf(System.currentTimeMillis())).toJobParameters();

    JobExecution executionVehicle = basicBatchConfigurer.getJobLauncher()
        .run(vehicleJob, param);

    JobExecution executionJourney = basicBatchConfigurer.getJobLauncher()
        .run(journeyJob, param);

    LOGGER.info("Job Vehicle finished with status : " + executionVehicle.getStatus());
    LOGGER.info("Job Journey finished with status : " + executionJourney.getStatus());
  }

}
