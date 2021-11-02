package com.michelin.synchro.batch;

import com.michelin.synchro.batch.model.Journey;
import com.michelin.synchro.batch.model.Vehicle;
import com.michelin.synchro.batch.repository.JourneyRepository;
import com.michelin.synchro.batch.repository.VehicleRepository;
import java.util.Date;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BasicBatchConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfig {

    private static final Logger LOGGER = getLogger(BatchConfig.class);

    @Value("${batch.vehicle.json-path}")
    private String vehicleJsonPath;

    @Value("${batch.journey.json-path}")
    private String journeyJsonPath;

    @Value("${batch.chunk:100}")
    private int chunk;

    @Autowired
    private BasicBatchConfigurer basicBatchConfigurer;

    @Autowired
    private JobCompletionNotificationListener listener;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private Step vehicleStep;

    @Autowired
    private Step journeyStep;

    /**
     * Cron pour executer l'opération de lecture - ecriture
     *
     * @throws Exception
     */
    @Scheduled(cron = "${batch.read-write.cron}")
    public void runReadWrite() throws Exception {

        LOGGER.info("Job Started at :" + new Date());

        JobParameters param = new JobParametersBuilder().addString("JobID",
            String.valueOf(System.currentTimeMillis())).toJobParameters();

        JobExecution executionVehicle = basicBatchConfigurer.getJobLauncher()
            .run(vehicleJob(), param);

        JobExecution executionJourney = basicBatchConfigurer.getJobLauncher()
            .run(journeyJob(), param);

        LOGGER.info("Job Vehicle finished with status : " + executionVehicle.getStatus());
        LOGGER.info("Job Journey finished with status : " + executionJourney.getStatus());
    }

    @Bean
    public Job vehicleJob() {
        return jobBuilderFactory.get("vehicleJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .start(vehicleStep)
            .build();
    }

    @Bean
    public Job journeyJob() {
        return jobBuilderFactory.get("journeyJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .start(journeyStep)
            .build();
    }

    @Bean("vehicleStep")
    public Step vehicleStep(PlatformTransactionManager platformTransactionManager,
        JsonFileListItemReader vehicleJsonReader,
        ItemWriter<Vehicle> writer) {
        return stepBuilderFactory
            .get("vehicleStep")
            .transactionManager(platformTransactionManager)
            .<Vehicle, Vehicle>chunk(chunk)
            .reader(vehicleJsonReader)
            .writer(writer)
            .build();
    }

    @Bean("journeyStep")
    public Step journeyStep(PlatformTransactionManager platformTransactionManager,
        JsonFileListItemReader journeyJsonReader,
        ItemWriter<Journey> writer) {
        return stepBuilderFactory
            .get("journeyStep")
            .transactionManager(platformTransactionManager)
            .<Journey, Journey>chunk(chunk)
            .reader(journeyJsonReader)
            .writer(writer)
            .build();
    }

    @Bean("vehicleJsonReader")
    public JsonFileListItemReader vehicleJsonReader() {
        FileSystemResource resource = new FileSystemResource(vehicleJsonPath);
        JsonFileListItemReader reader = new JsonFileListItemReader();
        reader.setResource(resource);
        reader.setClassToBound(Vehicle.class.getCanonicalName());
        return reader;
    }

    @Bean("journeyJsonReader")
    public JsonFileListItemReader journeyJsonReader() {
        FileSystemResource resource = new FileSystemResource(journeyJsonPath);
        JsonFileListItemReader reader = new JsonFileListItemReader();
        reader.setResource(resource);
        reader.setClassToBound(Journey.class.getCanonicalName());
        return reader;
    }

    @Bean
    public ItemWriter<Vehicle> vehicleItemWriter(VehicleRepository vehicleRepository) {
        return new DataWriter<>(vehicleRepository);
    }

    @Bean
    public ItemWriter<Journey> journeyItemWriter(JourneyRepository journeyRepository) {
        return new DataWriter<>(journeyRepository);
    }

    @Bean
    public JobExecutionListenerSupport jobExecutionListenerSupport() {
        return new JobExecutionListenerSupport() {
            @Override
            public void afterJob(org.springframework.batch.core.JobExecution jobExecution) {
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    LOGGER.info("!!! JOB FINISHED !!! ");
                }
            }
        };
    }
}
