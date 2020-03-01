package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.List;

public class ClassMetadata extends AnnotatedMetadata {

  public boolean simpleType;
  public List<FieldMetadata> fields = new ArrayList<>();

  public ClassMetadata(int id) {
    super(id);
  }
}
