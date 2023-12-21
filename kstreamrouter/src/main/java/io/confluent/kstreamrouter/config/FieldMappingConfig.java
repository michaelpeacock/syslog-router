package io.confluent.kstreamrouter.config;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import io.confluent.kstreamrouter.model.FieldMapping;

@Validated
@Configuration
@ConfigurationProperties(prefix = "fieldmapping")
public class FieldMappingConfig {
  private static final Logger logger = LoggerFactory.getLogger(FieldMappingConfig.class);
  
  private List<FieldMapping> mappings = new ArrayList<>();
  
  public List<FieldMapping> getMappings() {
    return this.mappings;
  }
  
  public void setMappings(List<FieldMapping> mappings) {
    this.mappings = mappings;
  }
}
