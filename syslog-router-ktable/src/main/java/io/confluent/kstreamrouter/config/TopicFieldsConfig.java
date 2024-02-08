package io.confluent.kstreamrouter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.confluent.kstreamrouter.model.TopicFields;

//@Validated
@Configuration
@ConfigurationProperties(prefix = "topic")
public class TopicFieldsConfig {
    private static final Logger logger = LoggerFactory.getLogger(CustomFieldsConfig.class);

    private TopicFields settings;

    public TopicFields getSettings() {
        return settings;
    }

    public void setSettings(TopicFields settings) {
        this.settings = settings;
    }

}