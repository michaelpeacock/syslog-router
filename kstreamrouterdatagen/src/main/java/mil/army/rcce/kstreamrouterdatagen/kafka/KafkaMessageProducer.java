package mil.army.rcce.kstreamrouterdatagen.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageProducer {

    Logger logger = LoggerFactory.getLogger(KafkaMessageProducer.class);

    @Value("${application.configs.syslog.input.topic.name}")
    private String SYSLOG_INPUT_TOPIC_NAME;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /*
      Publishes test messages on to the syslog input topic.
      Topic name specified in application yaml file
     */
    public void generateLogEvent(JsonNode logMessage) {
        logger.info(String.format("Producing Message: %s", logMessage));
        kafkaTemplate.send(SYSLOG_INPUT_TOPIC_NAME, logMessage);
    }


}
