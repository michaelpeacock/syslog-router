package mil.army.rcc.kstreamrouter.model;

import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import java.awt.TrayIcon.MessageType;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Represents a standard syslog message.
 */
public class SyslogMessage {
  LocalDateTime date;
  LocalDateTime receivedDate;
  InetAddress remoteAddress;
  String rawMessage;
  MessageType type;
  Integer level;
  Integer version;
  Integer facility;
  String host;
  String message;
  String processId;
  String tag;
  String messageId;
  String appName;
  List<StructuredData> structuredData;
  String deviceVendor;
  String deviceProduct;
  String deviceVersion;
  String deviceEventClassId;
  String name;
  String severity;
  Map<String, String> extension;

  interface StructuredData {

    String id();

    Map<String, String> structuredDataElements();
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public LocalDateTime getReceivedDate() {
    return receivedDate;
  }

  public void setReceivedDate(LocalDateTime receivedDate) {
    this.receivedDate = receivedDate;
  }

  public InetAddress getRemoteAddress() {
    return remoteAddress;
  }

  public void setRemoteAddress(InetAddress remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  public String getRawMessage() {
    return rawMessage;
  }

  public void setRawMessage(String rawMessage) {
    this.rawMessage = rawMessage;
  }

  public MessageType getType() {
    return type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Integer getFacility() {
    return facility;
  }

  public void setFacility(Integer facility) {
    this.facility = facility;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getProcessId() {
    return processId;
  }

  public void setProcessId(String processId) {
    this.processId = processId;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public List<StructuredData> getStructuredData() {
    return structuredData;
  }

  public void setStructuredData(
      List<StructuredData> structuredData) {
    this.structuredData = structuredData;
  }

  public String getDeviceVendor() {
    return deviceVendor;
  }

  public void setDeviceVendor(String deviceVendor) {
    this.deviceVendor = deviceVendor;
  }

  public String getDeviceProduct() {
    return deviceProduct;
  }

  public void setDeviceProduct(String deviceProduct) {
    this.deviceProduct = deviceProduct;
  }

  public String getDeviceVersion() {
    return deviceVersion;
  }

  public void setDeviceVersion(String deviceVersion) {
    this.deviceVersion = deviceVersion;
  }

  public String getDeviceEventClassId() {
    return deviceEventClassId;
  }

  public void setDeviceEventClassId(String deviceEventClassId) {
    this.deviceEventClassId = deviceEventClassId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public Map<String, String> getExtension() {
    return extension;
  }

  public void setExtension(Map<String, String> extension) {
    this.extension = extension;
  }

  @Override
  public String toString() {
    return "SyslogMessage{" +
        "date=" + date +
        ", receivedDate=" + receivedDate +
        ", remoteAddress=" + remoteAddress +
        ", rawMessage='" + rawMessage + '\'' +
        ", type=" + type +
        ", level=" + level +
        ", version=" + version +
        ", facility=" + facility +
        ", host='" + host + '\'' +
        ", message='" + message + '\'' +
        ", processId='" + processId + '\'' +
        ", tag='" + tag + '\'' +
        ", messageId='" + messageId + '\'' +
        ", appName='" + appName + '\'' +
        ", structuredData=" + structuredData +
        ", deviceVendor='" + deviceVendor + '\'' +
        ", deviceProduct='" + deviceProduct + '\'' +
        ", deviceVersion='" + deviceVersion + '\'' +
        ", deviceEventClassId='" + deviceEventClassId + '\'' +
        ", name='" + name + '\'' +
        ", severity='" + severity + '\'' +
        ", extension=" + extension +
        '}';
  }

  public static Serde<SyslogMessage> getJsonSerde(){
    Map<String, Object> serdeProps = new HashMap<>();
    serdeProps.put("json.value.type", SyslogMessage.class);
    final Serializer<SyslogMessage> detectionSer = new KafkaJsonSerializer<>();
    detectionSer.configure(serdeProps, false);

    final Deserializer<SyslogMessage> detectionDes = new KafkaJsonDeserializer<>();
    detectionDes.configure(serdeProps, false);
    return Serdes.serdeFrom(detectionSer, detectionDes);
  }

}
