package io.confluent.kstreamrouter.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.confluent.kstreamrouter.KstreamrouterApplication;
import io.confluent.kstreamrouter.config.CustomFieldsConfig;
import io.confluent.kstreamrouter.config.FieldMappingConfig;
import io.confluent.kstreamrouter.config.RouterProperties;
import io.confluent.kstreamrouter.config.RoutingConfig;
import io.confluent.kstreamrouter.model.CustomFields;
import io.confluent.kstreamrouter.model.FieldMapping;
import io.confluent.kstreamrouter.model.JsonUtils;
import io.confluent.kstreamrouter.model.RoutingRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

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
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

@Service
public class MessageRoutingService {
    private static final Logger logger = LoggerFactory.getLogger(MessageRoutingService.class);

    @Autowired
    RoutingConfig routingConfig;

    @Autowired
    CustomFieldsConfig customFields;

    @Autowired
    FieldMappingConfig fieldMappings;

    @Autowired
    RouterProperties routerProperties;

    @Value("${routing.default.inputTopic}")
    private String inputTopic;
 
    @Value("${routing.default.inputTopicPatternField}")
    private String inputTopicPatternField;

    @Value("${routing.default.outputTopic}")
    private String defaultOutputTopic;

    @Value("#{new Boolean('${routing.default.outputAllFields:false}')}")
    private Boolean outputAllFields;

    private KafkaStreams streams;
    private AdminClient client = null;
    private Collection<String> topicList = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();


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
        
        // check to see if the attribute in the input topic is being remapped
        for (FieldMapping field : this.fieldMappings.getMappings()) {
            if (field.getCurrentName().matches(inputTopicPatternField)) {
                inputTopicPatternField = field.getMappedName();
            }
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
        KStream<String, JsonNode> kStream = builder.stream(topicList,
            Consumed.with(Serdes.String(), JsonUtils.getJsonSerde()));

        kStream 
            //.peek((k, v) -> System.out.println("Consumed Message::: " + " Key="+k + " Value=" + v))
            .mapValues((v) -> updateSyslogValues(v))
            .to(syslogTopicNameExtractor, Produced.with(Serdes.String(), JsonUtils.getJsonSerde()));

        return builder.build();
    }

    /*
       This function will iterate over the CustomFields and add the name/value pairs set in the
       routingconfig.yaml file.
     */
    private JsonNode updateSyslogValues(JsonNode syslogValues) {
        JsonNode jsonNode = this.mapper.createObjectNode();

        if (this.outputAllFields.booleanValue() == true)
            jsonNode = syslogValues; 
        
        for (FieldMapping field : this.fieldMappings.getMappings()) {
            if (syslogValues.has(field.getCurrentName())) {
                JsonNode fieldValue = syslogValues.get(field.getCurrentName());
                if (jsonNode.has(field.getCurrentName())) {
                    ((ObjectNode)jsonNode).remove(field.getCurrentName()); 
                }
                
                ((ObjectNode)jsonNode).put(field.getMappedName(), fieldValue.asText());
            } 
        } 
        
        for (CustomFields custom : this.customFields.getCustomFields()) {
            ((ObjectNode)jsonNode).put(custom.getName(), custom.getValue()); 
        }

        return jsonNode;
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
        List<RoutingRule> routingRuleList = routingConfig.getRules();

        System.out.println("logEvent: " + logEvent);
        System.out.println("inputTopicPatternField: " + inputTopicPatternField);

        if (logEvent.has(inputTopicPatternField)) {
            for (RoutingRule routingRuleObj : routingRuleList) {
                if(routingRuleObj.getPattern().matcher(logEvent.get(inputTopicPatternField).toString()).matches()) {
                    outTopic = routingRuleObj.getOutputTopic();
                    break;
                }
            }
        }

        if(null == outTopic) {
            System.out.println("No Matching Pattern Found for Event...Routing to default output topic");
            outTopic = defaultOutputTopic;
        }

        System.out.println("Event is being routed to Output Topic:" + outTopic);
        return outTopic;
    };


    public static void main(String[] args) {
		System.out.println("in main");
        Pattern pattern = Pattern.compile(".*NetScreen.*");
		if(pattern.matcher((": NetScreen: EventPriority: ").toString()).matches()) {
            System.out.println("match found");
        }
	}
}
