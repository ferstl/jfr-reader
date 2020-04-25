package com.github.ferstl.jfrreader.parser.metadata;

public class SettingMetadata extends AnnotatedMetadata {

  private SettingMetadata(SettingMetadataBuilder builder) {
    super(builder);
  }

  public static SettingMetadataBuilder builder(int id) {
    return new SettingMetadataBuilder(id);
  }

  public static class SettingMetadataBuilder extends AnnotatedMetadataBuilder {

    private SettingMetadata instance;

    public SettingMetadataBuilder(int id) {
      super(id);
    }

    @Override
    public SettingMetadata build() {
      if (this.instance == null) {
        this.instance = new SettingMetadata(this);
      }

      return this.instance;
    }
  }
}
