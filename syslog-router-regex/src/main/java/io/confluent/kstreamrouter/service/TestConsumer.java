package io.confluent.kstreamrouter.service;

import org.apache.kafka.clients.consumer.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestConsumer {

    public static void main(final String[] args) throws Exception {
        final ArrayList<String> topicList = new ArrayList<>();
        topicList.add("topic-busy");
        topicList.add("topic-trickle");

        // Load consumer configuration settings from a local file
        // Reusing the loadConfig method from the ProducerExample class

        // Add additional properties.
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-java-getting-started");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        try (final Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(topicList);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
 
                if (records.count() > 0) {
                    System.out.println("Calling poll() number of records " + records.count());
                    Map<String, Integer> topicCount = new HashMap<>();

                    for (ConsumerRecord<String, String> record : records) {
                           String topic = record.topic();
                           if (topicCount.containsKey(topic)) {
                                topicCount.put(topic, (topicCount.get(topic) + 1));
                           } else {
                                topicCount.put(topic, 1);
                           }

                    }

                    topicCount.forEach((k, v) -> System.out.println("Read " + v + " records from " + k + "topic"));
                }

                // for (ConsumerRecord<String, String> record : records) {
                //     String key = record.key();
                //     String value = record.value();
                //     String topic = record.topic();
                //     System.out.println(
                //             String.format("Consumed event from topic %s", topic));
                // }
            }
        }
    }

}
