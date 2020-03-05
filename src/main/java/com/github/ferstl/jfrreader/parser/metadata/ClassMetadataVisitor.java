package com.github.ferstl.jfrreader.parser.metadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ClassMetadataVisitor implements MetadataNodeVisitor {

  public Map<String, EventMetadata> events = new HashMap<>();
  public Map<String, AnnotationMetadata> annotations = new HashMap<>();
  public Map<String, ClassMetadata> classes = new HashMap<>();
  public Map<String, SettingMetadata> settings = new HashMap<>();

  @Override
  public void visit(MetadataNode metadataNode) {
    if (!"class".equals(metadataNode.name)) {
      return;
    }

    String superType = metadataNode.attributes.getOrDefault("superType", "n/a");
    switch (superType) {
      case "java.lang.annotation.Annotation":
        processAnnotation(metadataNode);
        break;
      case "jdk.jfr.Event":
        processEvent(metadataNode);
        break;
      case "jdk.jfr.SettingControl":
        processSetting(metadataNode);
        break;
      default:
        processClass(metadataNode);
    }
  }

  private void processAnnotation(MetadataNode metadataNode) {
    String id = metadataNode.attributes.get("id");
    AnnotationMetadata annotation = this.annotations.computeIfAbsent(id, key -> new AnnotationMetadata(Integer.parseInt(key)));
    annotation.name = metadataNode.attributes.get("name");

    for (MetadataNode child : metadataNode.children) {
      switch (child.name) {
        case "annotation":
          annotation.annotations.add(createAnnotationValue(child));
          break;
        case "field":
          annotation.fields.add(createField(child));
      }
    }
  }

  private void processEvent(MetadataNode metadataNode) {
    String id = metadataNode.attributes.get("id");
    EventMetadata eventMetadata = this.events.computeIfAbsent(id, key -> new EventMetadata(Integer.parseInt(id)));
    eventMetadata.name = metadataNode.attributes.get("name");

    for (MetadataNode child : metadataNode.children) {
      switch (child.name) {
        case "field":
          eventMetadata.fields.add(createField(child));
          break;
        case "setting":
          eventMetadata.settings.add(crateSettingValue(child));
      }
    }
  }

  private void processSetting(MetadataNode metadataNode) {
    String id = metadataNode.attributes.get("id");
    SettingMetadata settingMetadata = this.settings.computeIfAbsent(id, key -> new SettingMetadata(Integer.parseInt(id)));
    settingMetadata.name = metadataNode.attributes.get("name");

    for (MetadataNode child : metadataNode.children) {
      if ("annotation".equals(child.name)) {
        settingMetadata.annotations.add(createAnnotationValue(child));
      }
    }
  }

  private void processClass(MetadataNode metadataNode) {
    String id = metadataNode.attributes.get("id");
    ClassMetadata classMetadata = this.classes.computeIfAbsent(id, key -> new ClassMetadata(Integer.parseInt(id)));
    classMetadata.name = metadataNode.attributes.get("name");
    classMetadata.simpleType = Boolean.parseBoolean(metadataNode.attributes.getOrDefault("simpleType", "false"));

    for (MetadataNode child : metadataNode.children) {
      switch (child.name) {
        case "annotation":
          classMetadata.annotations.add(createAnnotationValue(child));
          break;
        case "field":
          classMetadata.fields.add(createField(child));
          break;
      }
    }
  }

  private SettingValue crateSettingValue(MetadataNode child) {
    SettingValue value = new SettingValue();
    String metadataId = child.attributes.get("class");

    value.metadata = this.settings.computeIfAbsent(metadataId, key -> new SettingMetadata(Integer.parseInt(key)));
    for (Entry<String, String> entry : child.attributes.entrySet()) {
      String attributeName = entry.getKey();
      String attributeValue = entry.getValue();

      switch (attributeName) {
        case "name":
          value.name = attributeValue;
          break;
        case "defaultValue":
          value.defaultValue = attributeValue;
          break;
      }
    }

    return value;
  }

  private AnnotationValue createAnnotationValue(MetadataNode child) {
    AnnotationValue value = new AnnotationValue();
    String annotationTypeId = child.attributes.get("class");

    value.metadata = this.annotations.computeIfAbsent(annotationTypeId, key -> new AnnotationMetadata(Integer.parseInt(key)));
    for (Entry<String, String> entry : child.attributes.entrySet()) {
      String attributeName = entry.getKey();
      String attributeValue = entry.getValue();

      if ("class".equals(attributeName)) {
        continue;
      }

      if (attributeValue.contains("-")) {
        value.addValue(attributeName, attributeValue.substring(0, attributeValue.lastIndexOf('-')));
      } else {
        value.addValue(attributeName, attributeValue);
      }
    }

    return value;
  }

  private FieldMetadata createField(MetadataNode child) {
    String name = child.attributes.get("name");
    String typeIdStr = child.attributes.get("class");

    int typeId = Integer.parseInt(typeIdStr);
    FieldMetadata field = new FieldMetadata(typeId);

    field.type = this.classes.computeIfAbsent(typeIdStr, key -> new ClassMetadata(typeId));
    field.name = name;
    field.constantPool = Boolean.parseBoolean(child.attributes.getOrDefault("constantPool", "false"));

    return field;
  }
}
