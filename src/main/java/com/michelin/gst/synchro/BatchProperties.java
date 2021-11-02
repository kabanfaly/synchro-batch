package com.michelin.gst.synchro;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "batch")
@ConstructorBinding
public class BatchProperties {

  public final Integer chunk;

  public final String vehicleJsonPath;

  public final String journeyJsonPath;

  public BatchProperties(Integer chunk, String vehicleJsonPath, String journeyJsonPath) {
    this.chunk = chunk;
    this.vehicleJsonPath = vehicleJsonPath;
    this.journeyJsonPath = journeyJsonPath;
  }
}
