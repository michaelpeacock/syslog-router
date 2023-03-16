package mil.army.rcc.kstreamrouter.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.function.Consumer;
import mil.army.rcc.kstreamrouter.config.CustomFieldsConfig;
import mil.army.rcc.kstreamrouter.config.RoutingConfig;
import mil.army.rcc.kstreamrouter.model.CustomFields;
import mil.army.rcc.kstreamrouter.model.RoutingRule;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

@Service
public class MessageRoutingService {

    private static final Logger logger = LoggerFactory.getLogger(MessageRoutingService.class);

    @Autowired
    RoutingConfig routingConfig;

    @Autowired
    CustomFieldsConfig customFields;

    @Value("${routing.default.topic}")
    private String defaultRoutingTopicName;


    /*
     Function to route String messages based on
            a) RegEx patterns
            b) based on RegEx patterns and output topics from properties file
        Input: A Stream with key, value as String
        Output: Returns nothing. Publishes consumed message as-is to the destination topic determined by Routing Rules

        NOTE:: Output Topics need to be created outside this application.
     */
    @Bean
    public Consumer<KStream<String, JsonNode>> dynamicRouteSyslogMessages() {

        return event -> {
            event.peek((k, v) -> logger.debug("Consumed Message::: " + " Key="+k + " Value=" + v.toString()))
                .mapValues((v) -> addCustomFileds(v))
                .to(syslogTopicNameExtractor, Produced.with(Serdes.String(), new JsonSerde<>(JsonNode.class)));
        };
    }

    /*
       This function will iterate over the CustomFields and add the name/value pairs set in the
       routingconfig.yaml file.
     */
    private JsonNode addCustomFileds(JsonNode syslogValues) {
        JsonNode updatedValues = syslogValues;

        for (CustomFields custom : customFields.getCustomFields()) {
            ((ObjectNode) updatedValues).put(custom.getName(), custom.getValue());
        }

        return updatedValues;
    }

    /*
       This function will iterate over the RoutingRules and match with the consumed message value.
       On Match, it will break from the loop and return the associated topic name to route message to.
       If no match is found it will return the default topic name provided in application yaml
     */
    final TopicNameExtractor<String, JsonNode> syslogTopicNameExtractor = (key, logEvent, recordContext) -> {
        String outTopic = null;
        //ObjectMapper mapper = new ObjectMapper();
        //SyslogMessage logEventData = mapper.convertValue(logEvent, SyslogMessage.class);
        logger.trace("Matching Pattern for Event:" + logEvent.toString());
        List<RoutingRule> routingRuleList = routingConfig.getRules();

        for (RoutingRule routingRuleObj : routingRuleList) {
              if(routingRuleObj.getPattern().matcher(logEvent.get("rawMessage").toString()).matches()) {
                outTopic = routingRuleObj.getOutputTopic();
                break;
            }
        }

        if(null == outTopic) {
            logger.trace("No Matching Pattern Found for Event...Routing to default output topic");
            outTopic = defaultRoutingTopicName;
        }

        logger.debug("Event is being routed to Output Topic:::" + outTopic);
        return outTopic;
    };

}
