package io.confluent.kstreamrouter.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.confluent.kstreamrouter.model.CustomFields;

//@Validated
@Configuration
@ConfigurationProperties(prefix = "custom")
public class CustomFieldsConfig {
    private static final Logger logger = LoggerFactory.getLogger(CustomFieldsConfig.class);

    private List<CustomFields> customFields = new ArrayList<>();

    public List<CustomFields> getCustomFields() {
        return customFields;
    }

    public void setRules(List<CustomFields> customFields) {
        this.customFields = customFields;
    }
}