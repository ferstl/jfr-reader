package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import com.github.ferstl.jfrreader.parser.metadata.AnnotationMetadata.AnnotationMetadataBuilder;
import static java.util.Collections.unmodifiableMap;

public class AnnotationValue {

  public AnnotationMetadata metadata;
  // TODO Use ClassInstance? Or introduce AnnotationInstance?
  private final Map<String, List<String>> values;

  public AnnotationValue(AnnotationValueBuilder builder) {
    this.metadata = builder.metadata.build();

    Map<String, List<String>> valueEntries = new LinkedHashMap<>();
    for (Entry<String, List<String>> entry : builder.values.entrySet()) {
      valueEntries.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    this.values = unmodifiableMap(valueEntries);
  }

  public static AnnotationValueBuilder builder(AnnotationMetadataBuilder metadata) {
    return new AnnotationValueBuilder(metadata);
  }

  public static class AnnotationValueBuilder {

    private final AnnotationMetadataBuilder metadata;
    private final Map<String, List<String>> values = new LinkedHashMap<>();
    private AnnotationValue instance;

    public AnnotationValueBuilder(AnnotationMetadataBuilder metadata) {
      Objects.requireNonNull(metadata, "Metadata must not be null");
      this.metadata = metadata;
    }

    public AnnotationValueBuilder value(String attributeName, String value) {
      List<String> values = this.values.computeIfAbsent(attributeName, key -> new ArrayList<>());
      values.add(value);
      return this;
    }

    public AnnotationValue build() {
      if (this.instance == null) {
        this.instance = new AnnotationValue(this);
      }

      return this.instance;
    }
  }
}
