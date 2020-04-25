package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.List;
import com.github.ferstl.jfrreader.parser.metadata.FieldMetadata.FieldMetadataBuilder;
import static java.util.stream.Collectors.toList;

public class AnnotationMetadata extends AnnotatedMetadata {

  private final List<FieldMetadata> fields;

  private AnnotationMetadata(AnnotationMetadataBuilder builder) {
    super(builder);
    this.fields = builder.fields.stream()
        .map(FieldMetadataBuilder::build)
        .collect(toList());
  }

  public static AnnotationMetadataBuilder builder(int id) {
    return new AnnotationMetadataBuilder(id);
  }

  public List<FieldMetadata> getFields() {
    return this.fields;
  }

  public static class AnnotationMetadataBuilder extends AnnotatedMetadataBuilder {

    List<FieldMetadataBuilder> fields;
    private AnnotationMetadata instance;

    public AnnotationMetadataBuilder(int id) {
      super(id);
      this.fields = new ArrayList<>();
    }

    public AnnotationMetadataBuilder field(FieldMetadataBuilder field) {
      this.fields.add(field);
      return this;
    }

    @Override
    public AnnotationMetadata build() {
      if (this.instance == null) {
        this.instance = new AnnotationMetadata(this);
      }

      return this.instance;
    }
  }
}
