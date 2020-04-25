package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

public class ClassInstance {

  public final ClassMetadata metadata;
  private final Map<String, ClassInstance> fields;
  private final Map<String, List<ClassInstance>> listFields;
  public Object value;

  private ClassInstance(ClassInstanceBuilder builder) {
    this.metadata = builder.metadata;

    // The order of the maps must be preserved
    LinkedHashMap<String, ClassInstance> fieldMap = new LinkedHashMap<>(builder.fields.size());
    for (Entry<String, ClassInstanceBuilder> entry : builder.fields.entrySet()) {
      fieldMap.put(entry.getKey(), entry.getValue().build());
    }

    Map<String, List<ClassInstance>> listFieldMap = new LinkedHashMap<>(builder.listFields.size());
    for (Entry<String, List<ClassInstanceBuilder>> entry : builder.listFields.entrySet()) {
      List<ClassInstance> instances = entry.getValue().stream()
          .map(ClassInstanceBuilder::build)
          .collect(toList());
      listFieldMap.put(entry.getKey(), instances);
    }

    this.fields = unmodifiableMap(fieldMap);
    this.listFields = unmodifiableMap(listFieldMap);
    this.value = builder.value;
  }

  public static ClassInstanceBuilder builder(ClassMetadata metadata) {
    return new ClassInstanceBuilder(metadata);
  }

  public Map<String, ClassInstance> getFields() {
    return this.fields;
  }

  public Map<String, List<ClassInstance>> getListFields() {
    return this.listFields;
  }

  public static final class ClassInstanceBuilder {

    private final ClassMetadata metadata;
    private final Map<String, ClassInstanceBuilder> fields;
    private final Map<String, List<ClassInstanceBuilder>> listFields;
    private Object value;
    private ClassInstance instance;

    public ClassInstanceBuilder(ClassMetadata metadata) {
      Objects.requireNonNull(metadata, "Metadata must not be null");
      this.metadata = metadata;
      this.fields = new LinkedHashMap<>();
      this.listFields = new LinkedHashMap<>();
    }

    public ClassInstanceBuilder field(String name, ClassInstanceBuilder field) {
      FieldMetadata fieldMetadata = this.metadata.getField(name);
      // TODO Check if fieldValue's type matches with the fildMetadata's type
      if (fieldMetadata.isList()) {
        List<ClassInstanceBuilder> listField = this.listFields.computeIfAbsent(name, key -> new ArrayList<>());
        listField.add(field);
      } else {
        this.fields.put(name, field);
      }

      return this;
    }

    public ClassInstanceBuilder value(Object value) {
      // TODO Verifiy fields when value is set.
      this.value = value;
      return this;
    }

    public ClassInstance build() {
      if (this.instance == null) {
        this.instance = new ClassInstance(this);
      }

      return this.instance;
    }
  }
}
