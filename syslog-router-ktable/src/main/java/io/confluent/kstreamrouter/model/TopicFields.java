package io.confluent.kstreamrouter.model;

import javax.validation.constraints.NotNull;

public class TopicFields {
  @NotNull
  private String inputTopics;

  @NotNull
  private String inputTopicCompareField;

  @NotNull
  private String inputTableTopic;

  @NotNull
  private String outputTopicFormat;

  @NotNull
  private String outputTopicAppendField;

  @NotNull
  private String outputTopicAppendUnknown;

  private String outputTopicIncludeFields;

  @NotNull
  private Boolean outputAllFields = false;

  public String getInputTopics() {
    return inputTopics;
  }

  public void setInputTopics(String inputTopics) {
    this.inputTopics = inputTopics;
  }

  public String getInputTableTopic() {
    return inputTableTopic;
  }

  public void setInputTableTopic(String inputTableTopic) {
    this.inputTableTopic = inputTableTopic;
  }

  public String getInputTopicCompareField() {
    return inputTopicCompareField;
  }

  public void setInputTopicCompareField(String inputTopicCompareField) {
    this.inputTopicCompareField = inputTopicCompareField;
  }

  public String getOutputTopicFormat() {
    return outputTopicFormat;
  }

  public void setOutputTopicFormat(String outputTopicFormat) {
    this.outputTopicFormat = outputTopicFormat;
  }

  public String getOutputTopicAppendField() {
    return outputTopicAppendField;
  }

  public void setOutputTopicAppendField(String outputTopicAppendField) {
    this.outputTopicAppendField = outputTopicAppendField;
  }

  public String getOutputTopicAppendUnknown() {
    return outputTopicAppendUnknown;
  }

  public void setOutputTopicAppendUnknown(String outputTopicAppendUnknown) {
    this.outputTopicAppendUnknown = outputTopicAppendUnknown;
  }

  public String getOutputTopicIncludeFields() {
    return outputTopicIncludeFields;
  }

  public void setOutputTopicIncludeFields(String outputTopicIncludeFields) {
    this.outputTopicIncludeFields = outputTopicIncludeFields;
  }

  public Boolean getOutputAllFields() {
    return outputAllFields;
  }

  public void setOutputAllFields(Boolean outputAllFields) {
    this.outputAllFields = outputAllFields;
  }

  
}
