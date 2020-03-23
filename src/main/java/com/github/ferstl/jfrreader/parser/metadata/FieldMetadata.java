package com.github.ferstl.jfrreader.parser.metadata;

public class FieldMetadata extends AnnotatedMetadata {

  public ClassMetadata type;
  public boolean constantPool;
  private final int dimension;

  public FieldMetadata(int id, int dimension) {
    super(id);
    // TODO Allow only 0 and 1
    this.dimension = dimension;
  }

  public boolean isList() {
    return this.dimension == 1;
  }
}
