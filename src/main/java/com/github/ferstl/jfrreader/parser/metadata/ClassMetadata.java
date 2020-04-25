package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.github.ferstl.jfrreader.parser.metadata.FieldMetadata.FieldMetadataBuilder;

public class ClassMetadata extends AnnotatedMetadata {

  private final boolean simpleType;
  private final Map<String, FieldMetadata> fields;

  ClassMetadata(ClassMetadataBuilder builder) {
    super(builder);
    this.simpleType = builder.simpleType;

    // Set this instance on the builder in order to handle circular references
    builder.instance = this;

    Map<String, FieldMetadata> fieldMap = new LinkedHashMap<>();
    for (FieldMetadataBuilder fieldBuilder : builder.fields) {
      FieldMetadata field = fieldBuilder.build();
      fieldMap.put(field.name, field);
    }
    this.fields = Collections.unmodifiableMap(fieldMap);
  }

  public static ClassMetadataBuilder builder(int id) {
    return new ClassMetadataBuilder(id);
  }

  public FieldMetadata getField(String name) {
    FieldMetadata field = this.fields.get(name);
    if (field == null) {
      throw new IllegalArgumentException("Field '" + name + "' does not exist in '" + this.name + "'");
    }

    return field;
  }

  public Collection<FieldMetadata> getFields() {
    return this.fields.values();
  }

  public static class ClassMetadataBuilder extends AnnotatedMetadataBuilder {

    private final List<FieldMetadataBuilder> fields;

    private boolean simpleType;
    private ClassMetadata instance;

    public ClassMetadataBuilder(int id) {
      super(id);
      this.fields = new ArrayList<>();
    }

    public ClassMetadataBuilder simpleType(boolean simpleType) {
      this.simpleType = simpleType;
      return this;
    }

    public ClassMetadataBuilder field(FieldMetadataBuilder field) {
      this.fields.add(field);
      return this;
    }

    @Override
    public ClassMetadata build() {
      // TODO Make sure that no other builder methods may be invoked when instance is set
      if (this.instance == null) {
        // this.instance will be set in the constructor of ClassMetadata
        new ClassMetadata(this);
      }

      return this.instance;
    }
  }
}
