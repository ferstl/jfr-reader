package com.github.ferstl.jfrreader.parser.metadata;

public class FieldMetadata extends AnnotatedMetadata {

  public ClassMetadata type;
  public boolean constantPool;

  public FieldMetadata(int id) {
    super(id);
  }
}
