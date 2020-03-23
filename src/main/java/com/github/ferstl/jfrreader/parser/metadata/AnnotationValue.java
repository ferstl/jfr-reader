package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnnotationValue {

  public AnnotationMetadata metadata;
  // TODO Use ClassInstance? Or introduce AnnotationInstance?
  public Map<String, List<String>> values = new LinkedHashMap<>();

  public void addValue(String attributeName, String value) {
    List<String> values = this.values.computeIfAbsent(attributeName, key -> new ArrayList<>());
    values.add(value);
  }
}
