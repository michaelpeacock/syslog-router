# Getting Started

The kstreamsrouter application is a Kafka Streams application that take an input topic from the 
Confluent Syslog Connector and routes the data to sub-topics based on a set of regex rules.
Additionally, you have the option to add any custom fields to the routed topic data.

There are two types of syslog router applications. View the individual README files for setup and configuration.

* [syslog-router-regex](/syslog-router-regex) - routes messages to topics based on regex patterns
* [syslog-router-ktable](/syslog-router-ktable) - does a look up in a KTable and routes messages based on a field within the table