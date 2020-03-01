package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.List;

public class AnnotationMetadata extends AnnotatedMetadata {

  public List<FieldMetadata> fields = new ArrayList<>();

  public AnnotationMetadata(int id) {
    super(id);
  }
}
