package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.List;

public class AnnotatedMetadata extends BasicMetadata {

  public List<AnnotationValue> annotations = new ArrayList<>();

  public AnnotatedMetadata(int id) {
    super(id);
  }

}
