package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClassInstance {

  public final ClassMetadata metadata;
  private final Map<String, ClassInstance> fields = new LinkedHashMap<>();
  private final Map<String, List<ClassInstance>> listFields = new LinkedHashMap<>();
  public Object value;

  public ClassInstance(ClassMetadata metadata) {
    this.metadata = metadata;
  }

  public void addField(String name, ClassInstance fieldValue) {
    FieldMetadata fieldMetadata = this.metadata.getField(name);
    // TODO Check if fieldValue's type matches with the fildMetadata's type
    if (fieldMetadata.isList()) {
      List<ClassInstance> listField = this.listFields.computeIfAbsent(name, key -> new ArrayList<>());
      listField.add(fieldValue);
    } else {
      this.fields.put(name, fieldValue);
    }
  }
}
