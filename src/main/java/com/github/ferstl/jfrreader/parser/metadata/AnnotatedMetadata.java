package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.github.ferstl.jfrreader.parser.metadata.AnnotationValue.AnnotationValueBuilder;
import static java.util.stream.Collectors.toList;

public abstract class AnnotatedMetadata extends BasicMetadata {

  private final List<AnnotationValue> annotations;

  AnnotatedMetadata(AnnotatedMetadataBuilder builder) {
    super(builder);
    this.annotations = builder.annotations.stream()
        .map(AnnotationValueBuilder::build)
        .collect(toList());
  }

  public List<AnnotationValue> getAnnotations() {
    return this.annotations;
  }

  public static abstract class AnnotatedMetadataBuilder extends BasicMetadataBuilder {

    private final List<AnnotationValueBuilder> annotations;

    public AnnotatedMetadataBuilder(int id) {
      super(id);
      this.annotations = new ArrayList<>();
    }

    public AnnotatedMetadataBuilder annotation(AnnotationValueBuilder annotationValue) {
      Objects.requireNonNull(annotationValue, "Annotation value must not be null");
      this.annotations.add(annotationValue);
      return this;
    }

    @Override
    public abstract AnnotatedMetadata build();
  }
}
