package com.github.ferstl.jfrreader.parser.metadata;

public abstract class BasicMetadata {

  // * includes all primitive types * //
  // !!! Field has no ID !!! //
  public final int id;
  public String name;

  BasicMetadata(BasicMetadataBuilder builder) {
    this.id = builder.id;
    this.name = builder.name;
  }

  public static abstract class BasicMetadataBuilder {

    private final int id;
    private String name;

    public BasicMetadataBuilder(int id) {
      this.id = id;
    }

    public BasicMetadataBuilder name(String name) {
      this.name = name;
      return this;
    }

    public int getId() {
      return this.id;
    }

    public String getName() {
      return this.name;
    }

    public abstract BasicMetadata build();
  }
}
