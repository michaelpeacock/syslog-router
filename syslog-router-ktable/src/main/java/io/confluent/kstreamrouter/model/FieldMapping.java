package io.confluent.kstreamrouter.model;

import javax.validation.constraints.NotNull;

public class FieldMapping {
  @NotNull
  private String currentName;
  
  @NotNull
  private String mappedName;
  
  public String getCurrentName() {
    return this.currentName;
  }
  
  public void setCurrentName(String currentName) {
    this.currentName = currentName;
  }
  
  public String getMappedName() {
    return this.mappedName;
  }
  
  public void setMappedName(String mappedName) {
    this.mappedName = mappedName;
  }
}
