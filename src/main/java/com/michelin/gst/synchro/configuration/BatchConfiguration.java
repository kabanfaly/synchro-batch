package com.michelin.gst.synchro.configuration;

import com.michelin.gst.synchro.BatchProperties;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@EnableConfigurationProperties({ BatchProperties.class })
public class BatchConfiguration {}
