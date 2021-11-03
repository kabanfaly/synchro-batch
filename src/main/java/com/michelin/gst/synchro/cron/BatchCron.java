package com.michelin.gst.synchro.cron;

import com.michelin.gst.synchro.BatchProperties;
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
public class BatchCron {

    private static final Logger LOGGER = getLogger(BatchConfiguration.class);

    private final BasicBatchConfigurer basicBatchConfigurer;

    private final BatchProperties properties;

    private final Job vehicleJob;

    private final Job journeyJob;

    public BatchCron(BatchProperties properties, 
        BasicBatchConfigurer basicBatchConfigurer, Job vehicleJob, Job journeyJob) {
        this.properties = properties;
        this.basicBatchConfigurer = basicBatchConfigurer;
        this.vehicleJob = vehicleJob;
        this.journeyJob = journeyJob;
    }

    @Scheduled(cron = "${gst.synchro-cron}")
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

    @Scheduled(cron = "${gst.delete-achives-cron}")
    public void deleteArchives() {

    }

}
