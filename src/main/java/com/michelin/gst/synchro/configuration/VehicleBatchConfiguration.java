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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import static org.slf4j.LoggerFactory.getLogger;
import com.michelin.gst.synchro.dao.VehicleDAO;
import com.michelin.gst.synchro.file.FileArchiver;
import java.util.List;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.util.StringUtils;

@Configuration
public class VehicleBatchConfiguration {

    private static final Logger LOGGER = getLogger(VehicleBatchConfiguration.class);

    @Bean
    public Job vehicleJob(JobBuilderFactory jobBuilderFactory, 
        JobExecutionListenerSupport vehicleJobExecutionListenerSupport, 
        Step vehicleStep,
        Step moveVehiculeFileStep) {
        return jobBuilderFactory.get("vehicleJob")
            .incrementer(new RunIdIncrementer())
            .listener(vehicleJobExecutionListenerSupport)
            .start(vehicleStep)
            .next(moveVehiculeFileStep)
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

    @Bean
    public Step moveVehiculeFileStep(StepBuilderFactory stepBuilderFactory, Tasklet vehiculeFileArchiver) {
        return stepBuilderFactory.get("moveVehiculeFileStep")
            .tasklet(vehiculeFileArchiver)
            .build();
    }

    @Bean("vehicleJsonReader")
    public JsonFileListItemReader vehicleJsonReader(BatchProperties properties,@Value("azure-blob://<your-container-name>/<your-blob-name>") Resource resource) {
        FileSystemResource resource = new FileSystemResource(properties.vehicleJsonPath);
        JsonFileListItemReader reader = new JsonFileListItemReader();
        reader.setResource(resource);
        reader.setClassToBound(Vehicle.class.getCanonicalName());
        return reader;
    }

    @Bean
    public ItemWriter<Vehicle> vehicleItemWriter(VehicleDAO vehicleDAO) {
        return new DataWriter<>(vehicleDAO) {
            @Override
            public void write(List<? extends Vehicle> items) throws Exception {
                items.stream().forEachOrdered(item -> {
                    if (StringUtils.hasLength(item.getBrand()) && StringUtils.hasLength(item.getModel())) {
                        item.setBrandModel(item.getBrand() + item.getModel());
                    }
                    vehicleDAO.save(item);
                });
            }
        };
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

    @Bean
    public Tasklet vehiculeFileArchiver(BatchProperties properties) {
        return new FileArchiver(properties.vehicleJsonPath, properties.archivesDirectory);
    }
}
