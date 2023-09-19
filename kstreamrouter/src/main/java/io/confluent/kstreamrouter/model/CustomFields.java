package mil.army.rcc.kstreamrouter.model;

import javax.validation.constraints.NotNull;

public class CustomFields {
  @NotNull
  private String name;
  @NotNull
  private String value;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
