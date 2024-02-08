# Getting Started

The kstreamsrouter application is a Kafka Streams application that take an input topic from the 
Confluent Syslog Connector and routes the data to sub-topics based on a set of regex rules.
Additionally, you have the option to add any custom fields to the routed topic data.

There are two configuration files that need to be updated for your environment: `application.properties`
and `routingconfig.yaml`. Both files are located in the `config` directory.

### Application Configuration
The `application.properties` file contains all the properties specific to the application including broker and security settings.

```properties
application.id=syslog-router-app
bootstrap.servers=localhost:9092
enable.auto.commit=true
max.poll.records=5000
num.stream.threads=10

schema.registry=localhost:8081

# Security Settings
security.protocol=SSL
ssl.truststore.location=/etc/security/tls/kafka.client.truststore.jks
ssl.truststore.password=test1234
ssl.keystore.location=/etc/security/tls/kafka.client.keystore.jks
ssl.keystore.password=test1234
ssl.key.password=test1234
```

The following fields should be modified for your environment:
* `boostrap.servers` - list of comma separated Confluent brokers
* `schema.registry` - schema registry URL and port
* security settings

### Routing Configuration
The `routingconfig.yaml` file contains a list of custom fields and a list of regex patterns. The custom fields are optional and contain a list of name/value pairs that will be added to the output topic. If you do not want to include any custom fields, this section of the configuration file can be removed.

The routing section contains the following attributes:
* `inputTopic` - list of comma separated topic names
* `inputTopicPatternField` - the field in the topic to apply the regex pattern
* `outputTopic` - default output topic when no match is found
* `outputAllFields` - a flag to output all fields from the topic or just the custom and mapping fields
* `rules` -  and a list of regex pattern and specific output topic when a match is found


```yaml
routing:
  default:
    inputTopic: syslog
    inputTopicPatternField: "rawMessage"
    outputTopic: syslog-unknown
    outputAllFields: false
  rules:
    -
      regEx: ".*%ASA.*"
      outputTopic: "syslog-ips-cisco-asa"
    -
      regEx: ".*NetScreen.*"
      outputTopic: "syslog-fw-netscreen"
    -
      regEx: ".*AuditNotification.*"
      outputTopic: "syslog-misc-devices"
    -
      regEx: ".*%asa.*"
      outputTopic: "syslog-ips-cisco-asa"

custom:
  customFields:
    -
      name: "name"
      value: "My Name"
    -
      name: "site"
      value: "My Site"

fieldmapping:
  mappings:
    -
      currentName: "rawMessage"
      mappedName: "event.original"
    -
      currentName: "remoteAddress"
      mappedName: "log.syslog.hostname"
    -
      currentName: "facility"
      mappedName: "log.syslog.facility.code"
    -
      currentName: "processId"
      mappedName: "log.syslog.procid"
    -
      currentName: "technology"
      mappedName: "log.syslog.appname"
    -
      currentName: "timestamp"
      mappedName: "@timestamp"
```


#### Routing
The input topic is your raw syslog topic and can be updated by the `inputTopic` variable.

The routing works by applying a regex pattern to the topic. When a match is found, the data will get routed to the output topic defined in `outputTopic`. If no match is found, the data will get routed to the default topic defined in `default.outputTopic`. You can change the value of `outputTopic` defined in the `default` section. 

Items within the list are separated by the dash (`-`) symbol. To add a new regex pattern, add a `-` followed by a regex value and the output topic. Example:

```yaml
-
    regEx: ".*%ASA.*"
    outputTopic: syslog.ciscoasa.out"
```

#### Custom Fields
Custom fields allow you to add any custom fields/values to the output topic. Items within the list are separated by the dash (`-`) symbol. To add a new custom field, add a `-` followed by a name/value pairing. Example:

```yaml
-
    name: "myNewField"
    value: "myNewValue"
```

#### Field Mappings
Field mappings allow you to rename the fields within the output topic. The `currentName` and the `mappedName` is the new name. Items within the list are separated by the dash (`-`) symbol. To add a new custom field, add a `-` followed by a name/value pairing. Example:

```yaml
-
    currentName: "rawMessage"
    mappedName: "event.original"
```

***!!! IMPORTANT: All output topics, including the default output topic, should be created prior to running the streams application !!!***

### Running the Application
There is a script called `kstreams-router.sh` that will launch the Kafka Streams application. It is
configured to read the configuration files in the `config` directory. To run the application:

````
./kstreams-router.sh
````

***Note: if you are running the script from a remote directory, you may need to update the 
`SPRING_CONFIG_LOCATION` variable in the script to give the absolute path (i.e. replace `$PWD`).***