package com.github.ferstl.jfrreader.parser.metadata;

import com.github.ferstl.jfrreader.parser.metadata.ClassMetadata.ClassMetadataBuilder;

public class FieldMetadata extends AnnotatedMetadata {

  public ClassMetadata type;
  public boolean constantPool;
  private final int dimension;

  private FieldMetadata(FieldMetadataBuilder builder) {
    super(builder);
    this.type = builder.type != null ? builder.type : builder.typeBuilder.build();
    this.constantPool = builder.constantPool;
    this.dimension = builder.dimension;
  }

  public static FieldMetadataBuilder builder(ClassMetadataBuilder type) {
    return new FieldMetadataBuilder(type);
  }

  public boolean isList() {
    return this.dimension == 1;
  }

  public static class FieldMetadataBuilder extends AnnotatedMetadataBuilder {

    public ClassMetadataBuilder typeBuilder;
    private ClassMetadata type;
    public boolean constantPool;
    private int dimension;

    private FieldMetadata instance;

    public FieldMetadataBuilder(ClassMetadataBuilder type) {
      super(type.getId());
      this.typeBuilder = type;
    }

    public FieldMetadataBuilder constantPool(boolean constantPool) {
      this.constantPool = constantPool;
      return this;
    }

    public FieldMetadataBuilder dimension(int dimension) {
      this.dimension = dimension;
      return this;
    }

    @Override
    public FieldMetadata build() {
      if (this.instance == null) {
        this.instance = new FieldMetadata(this);
      }

      return this.instance;
    }
  }
}
