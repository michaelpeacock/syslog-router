package io.confluent.kstreamrouter.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.confluent.kstreamrouter.config.CustomFieldsConfig;
import io.confluent.kstreamrouter.config.RouterProperties;
import io.confluent.kstreamrouter.config.RoutingConfig;
import io.confluent.kstreamrouter.model.CustomFields;
import io.confluent.kstreamrouter.model.JsonUtils;
import io.confluent.kstreamrouter.model.RoutingRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageRoutingService {
    private static final Logger logger = LoggerFactory.getLogger(MessageRoutingService.class);

    @Autowired
    RoutingConfig routingConfig;

    @Autowired
    CustomFieldsConfig customFields;

    @Autowired
    RouterProperties routerProperties;

    @Value("${routing.default.inputTopic}")
    private String inputTopic;
 
    @Value("${routing.default.outputTopic}")
    private String defaultOutputTopic;

    private KafkaStreams streams;
    private AdminClient client = null;
    KStream<String, JsonNode> kStream = null;
    private Collection<String> topicList = new ArrayList<>();

    @PostConstruct
    private void initialize() {
        System.out.println("initializing streams app");
        Properties properties = new Properties(routerProperties.getProperties());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

        try {
            client = AdminClient.create(properties);
        } catch (KafkaException e) {
            logger.error(e.toString());
        }
        
        topicList = Arrays.asList(inputTopic.split("\\s*,\\s*"));

        Topology topology = createTopology();
        streams = new KafkaStreams(topology, routerProperties.getProperties());

        streams.cleanUp();
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    /*
     Function to route String messages based on
            a) RegEx patterns
            b) based on RegEx patterns and output topics from properties file
        Input: A Stream with key, value as String
        Output: Returns nothing. Publishes consumed message as-is to the destination topic determined by Routing Rules

        NOTE:: Output Topics need to be created outside this application.
     */
    public Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        kStream = builder.stream(topicList,
            Consumed.with(Serdes.String(), JsonUtils.getJsonSerde()));

        kStream.peek((k, v) -> System.out.println("Consumed Message::: " + " Key="+k + " Value=" + v))
            .mapValues((v) -> addCustomFileds(v))
            .to(syslogTopicNameExtractor, Produced.with(Serdes.String(), JsonUtils.getJsonSerde()));

        return builder.build();
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
            outTopic = defaultOutputTopic;
        }

        logger.debug("Event is being routed to Output Topic:::" + outTopic);
        return outTopic;
    };

}
