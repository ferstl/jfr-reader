package com.github.ferstl.jfrreader.parser.metadata;

import java.util.Objects;
import com.github.ferstl.jfrreader.parser.metadata.SettingMetadata.SettingMetadataBuilder;

public class SettingValue {

  private final SettingMetadata metadata;
  private final String name;
  private final String defaultValue;

  private SettingValue(SettingValueBuilder builder) {
    this.metadata = builder.metadata.build();
    this.name = builder.name;
    this.defaultValue = builder.defaultValue;
  }

  public static SettingValueBuilder builder(SettingMetadataBuilder metadata) {
    return new SettingValueBuilder(metadata);
  }

  public SettingMetadata getMetadata() {
    return this.metadata;
  }

  public String getName() {
    return this.name;
  }

  public String getDefaultValue() {
    return this.defaultValue;
  }

  public static class SettingValueBuilder {

    private final SettingMetadataBuilder metadata;
    private String name;
    private String defaultValue;
    private SettingValue instance;

    public SettingValueBuilder(SettingMetadataBuilder metadata) {
      Objects.requireNonNull(metadata, "Metadata must not be null");
      this.metadata = metadata;
    }

    public SettingValueBuilder name(String name) {
      this.name = name;
      return this;
    }

    public SettingValueBuilder defaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    public SettingValue build() {
      if (this.instance == null) {
        Objects.requireNonNull(this.name, "Name must not be null");
        this.instance = new SettingValue(this);
      }

      return this.instance;
    }
  }

}
