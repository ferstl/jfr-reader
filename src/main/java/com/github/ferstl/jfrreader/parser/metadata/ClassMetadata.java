package com.github.ferstl.jfrreader.parser.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassMetadata extends AnnotatedMetadata {

  public boolean simpleType;
  private final Map<String, FieldMetadata> fields = new LinkedHashMap<>();

  public ClassMetadata(int id) {
    super(id);
  }

  public void addField(FieldMetadata field) {
    this.fields.put(field.name, field);
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
}
