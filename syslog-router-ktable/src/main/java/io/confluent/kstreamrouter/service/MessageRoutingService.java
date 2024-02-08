package io.confluent.kstreamrouter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.confluent.kstreamrouter.config.CustomFieldsConfig;
import io.confluent.kstreamrouter.config.FieldMappingConfig;
import io.confluent.kstreamrouter.config.RouterProperties;
import io.confluent.kstreamrouter.config.TopicFieldsConfig;
import io.confluent.kstreamrouter.model.CustomFields;
import io.confluent.kstreamrouter.model.FieldMapping;
import io.confluent.kstreamrouter.model.JsonUtils;
import io.confluent.kstreamrouter.model.TopicFields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import javax.annotation.PostConstruct;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.avro.generic.GenericRecord;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;

@Service
public class MessageRoutingService {
    private static final Logger logger = LoggerFactory.getLogger(MessageRoutingService.class);

    @Autowired
    CustomFieldsConfig customFields;

    @Autowired
    FieldMappingConfig fieldMappings;

    @Autowired
    TopicFieldsConfig topicFields;

    @Autowired
    RouterProperties routerProperties;
    
    private KafkaStreams streams;
    private AdminClient client = null;
    private Collection<String> topicList = new ArrayList<>();
    private Collection<String> tableFields = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    private void initialize() {
        System.out.println("initializing streams app");
        Properties properties = (Properties)routerProperties.getProperties().clone();
        //properties.putAll(routerProperties.getProperties());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);

        try {
            client = AdminClient.create(properties);
        } catch (KafkaException e) {
            logger.error(e.toString());
        }

        topicList = Arrays.asList(topicFields.getSettings().getInputTopics().split("\\s*,\\s*"));
        tableFields = Arrays.asList(topicFields.getSettings().getOutputTopicIncludeFields().split("\\s*,\\s*"));

        Topology topology = createTopology();
        streams = new KafkaStreams(topology, properties);

        streams.cleanUp();
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    /*
     Function to route String messages based on
            a) match within a ktable
            b) based on matched fields and output topics defined in the routingconfig.yaml file
        Input: A Stream with or without a key, value as Avro
        Input: A KTable used for lookup from the key of the stream
        Output: Returns nothing. Publishes consumed message as-is to the destination topic determined by Routing Rules

        NOTE:: Output Topics need to be created outside this application in the routingconfig.yaml file.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Topology createTopology() {
        ObjectMapper mapper = new ObjectMapper();
        StreamsBuilder builder = new StreamsBuilder();
        TopicFields topicData =  topicFields.getSettings();
        
        KTable<String, String> assetInventoryTable = builder.table(topicData.getInputTableTopic(),
            Consumed.with(Serdes.String(), Serdes.String()));
        KStream<String, GenericRecord> kStream = builder.stream(topicList);

        // The topology will join the stream with a ktable, when doing a stream-table join, the key
        // of the message is evaluated with the key of the ktable. The following is done in the
        // topology:
        // - map - adds the key to the message of the stream
        // - leftJoin 
        //     - evaluates the key of the stream vs. the ktable key
        //     - if no match is found (asset is null), then a field is added to the stream (i.e. unknown)
        //     - if a match is found (asset is valid), then the value is added to the stream (i.e. technology)
        //     - optionally, additional fields can be added to the stream from the ktable (tableFields)
        // - map - another map is done to remove the key from the stream (prevents overloading a partition)
        // - to - routes the stream to a new output topic based on the fields in the syslog message
        kStream 
            //.peek((k, v) -> logger.debug("Consumed Message:::  Key=" + k + " Value=" + v.toString()))
            .map((k, v) -> {
                String newKey = "\"" + v.get(topicData.getInputTopicCompareField()) + "\"";
                try {
                    return KeyValue.pair(newKey, this.mapper.readTree(v.toString()));
                }
                catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }})
            .leftJoin(assetInventoryTable, (syslog, asset) -> {
                if (asset == null) {
                    logger.debug("asset is null");
                    ((ObjectNode)syslog).put(topicData.getOutputTopicAppendField(), topicData.getOutputTopicAppendUnknown());
                } else {
                    logger.debug("asset::: " + asset);
                        JsonNode assetJson;
                        try {
                            assetJson = this.mapper.readTree(asset);
                            ((ObjectNode)syslog).put(topicData.getOutputTopicAppendField(), 
                                assetJson.get(topicData.getOutputTopicAppendField()).asText());

                            //include any table fields
                            if (tableFields.size() != 0) {
                                tableFields.forEach((field) -> {
                                    if (assetJson.has(field)) {
                                       ((ObjectNode)syslog).put(field, assetJson.get(field).asText());
                                    }
                                });
                            }
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            ((ObjectNode)syslog).put(topicData.getOutputTopicAppendField(), topicData.getOutputTopicAppendUnknown());
                        }
                    }
                logger.info("syslog::: " + syslog);
                return syslog;
            }, Joined.with((Serde)Serdes.String(), (Serde)JsonUtils.getJsonSerde(), null))
            .map((k, v) -> KeyValue.pair(null, (Object)this.updateSyslogValues(JsonUtils.toJsonNode(v))))
            .to(this.syslogTopicNameExtractor, Produced.valueSerde((Serde)JsonUtils.getJsonSerde()));

        return builder.build();
    }

    // Adds in the fieldmappings and custom fields defined in the routingconfig.yaml
    private JsonNode updateSyslogValues(JsonNode syslogValues) {
        ObjectNode updatedValues = this.mapper.createObjectNode();
        if (topicFields.getSettings().getOutputAllFields().booleanValue()) {
            updatedValues = (ObjectNode) syslogValues;
        }

        for (FieldMapping field : this.fieldMappings.getMappings()) {
            if (!syslogValues.has(field.getCurrentName())) continue;
            
            JsonNode fieldValue = syslogValues.get(field.getCurrentName());
            if (updatedValues.has(field.getCurrentName())) {
                updatedValues.remove(field.getCurrentName());
            }
            updatedValues.put(field.getMappedName(), fieldValue.asText());
        }

        for (CustomFields custom : this.customFields.getCustomFields()) {
            updatedValues.put(custom.getName(), custom.getValue());
        }
        
        logger.debug("updateSyslogValues::: " + (JsonNode)updatedValues);
        return updatedValues;
    }

    // Routes the stream to the output topic based on the values in the syslog message and definition
    // routingconfig.yaml file
    @SuppressWarnings("unlikely-arg-type")
    final TopicNameExtractor<Object, JsonNode> syslogTopicNameExtractor = (key, syslog, recordContext) -> {
        TopicFields topicData =  topicFields.getSettings();
        String appendField = topicData.getOutputTopicAppendField();
        if (!this.customFields.getCustomFields().contains(topicData.getOutputTopicAppendField())) {
            for (FieldMapping field : this.fieldMappings.getMappings()) {
                if (!field.getCurrentName().equals(topicData.getOutputTopicAppendField())) continue;
                appendField = field.getMappedName();
                break;
            }
        }
        String outTopic = topicData.getOutputTopicFormat() + syslog.get(appendField).asText();
        logger.info("Event is being routed to Output Topic:::" + outTopic);
        return outTopic;
    };


}