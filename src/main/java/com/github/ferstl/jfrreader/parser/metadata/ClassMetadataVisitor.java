package com.github.ferstl.jfrreader.parser.metadata;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.github.ferstl.jfrreader.parser.metadata.AnnotationMetadata.AnnotationMetadataBuilder;
import com.github.ferstl.jfrreader.parser.metadata.AnnotationValue.AnnotationValueBuilder;
import com.github.ferstl.jfrreader.parser.metadata.ClassMetadata.ClassMetadataBuilder;
import com.github.ferstl.jfrreader.parser.metadata.EventMetadata.EventMetadataBuilder;
import com.github.ferstl.jfrreader.parser.metadata.FieldMetadata.FieldMetadataBuilder;
import com.github.ferstl.jfrreader.parser.metadata.SettingMetadata.SettingMetadataBuilder;
import com.github.ferstl.jfrreader.parser.metadata.SettingValue.SettingValueBuilder;

public class ClassMetadataVisitor implements MetadataNodeVisitor {

  private final Map<String, EventMetadataBuilder> events = new HashMap<>();
  private final Map<String, AnnotationMetadataBuilder> annotations = new HashMap<>();
  private final Map<String, ClassMetadataBuilder> classes = new HashMap<>();
  private final Map<String, SettingMetadataBuilder> settings = new HashMap<>();
  private Map<String, SettingMetadata> theSettings;
  private Map<String, ClassMetadata> theClasses;
  private Map<String, AnnotationMetadata> theAnnotations;
  private Map<String, EventMetadata> theEvents;

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

  // TODO clean up
  public void process() {
    Map<String, SettingMetadata> settings = new LinkedHashMap<>();
    for (Entry<String, SettingMetadataBuilder> entry : this.settings.entrySet()) {
      settings.put(entry.getKey(), entry.getValue().build());
    }
    this.theSettings = Map.copyOf(settings);

    Map<String, ClassMetadata> classes = new LinkedHashMap<>();
    for (Entry<String, ClassMetadataBuilder> entry : this.classes.entrySet()) {
      classes.put(entry.getKey(), entry.getValue().build());
    }

    this.theClasses = Map.copyOf(classes);

    Map<String, EventMetadata> events = new LinkedHashMap<>();
    for (Entry<String, EventMetadataBuilder> entry : this.events.entrySet()) {
      events.put(entry.getKey(), entry.getValue().build());
    }
    this.theEvents = Map.copyOf(events);

    Map<String, AnnotationMetadata> annotations = new LinkedHashMap<>();
    for (Entry<String, AnnotationMetadataBuilder> entry : this.annotations.entrySet()) {
      annotations.put(entry.getKey(), entry.getValue().build());
    }
    this.theAnnotations = Map.copyOf(annotations);
  }

  public Map<String, EventMetadata> getEvents() {
    return this.theEvents;
  }

  public Map<String, AnnotationMetadata> getAnnotations() {
    return this.theAnnotations;
  }

  public Map<String, ClassMetadata> getClasses() {
    return this.theClasses;
  }

  public Map<String, SettingMetadata> getSettings() {
    return this.theSettings;
  }

  private void processAnnotation(MetadataNode metadataNode) {
    String id = metadataNode.attributes.get("id");
    AnnotationMetadataBuilder annotation = this.annotations.computeIfAbsent(id, key -> AnnotationMetadata.builder(Integer.parseInt(key)));
    annotation.name(metadataNode.attributes.get("name"));

    for (MetadataNode child : metadataNode.children) {
      switch (child.name) {
        case "annotation":
          annotation.annotation(createAnnotationValue(child));
          break;
        case "field":
          annotation.fields.add(createField(child));
      }
    }
  }

  private void processEvent(MetadataNode metadataNode) {
    String id = metadataNode.attributes.get("id");
    EventMetadata.builder(1);
    EventMetadataBuilder eventMetadata = this.events.computeIfAbsent(id, key -> EventMetadata.builder(Integer.parseInt(id)));
    eventMetadata.name(metadataNode.attributes.get("name"));

    for (MetadataNode child : metadataNode.children) {
      switch (child.name) {
        case "field":
          eventMetadata.field(createField(child));
          break;
        case "setting":
          eventMetadata.setting(createSettingValue(child));
      }
    }
  }

  private void processSetting(MetadataNode metadataNode) {
    String id = metadataNode.attributes.get("id");
    SettingMetadataBuilder settingMetadata = this.settings.computeIfAbsent(id, key -> SettingMetadata.builder(Integer.parseInt(id)));
    settingMetadata.name(metadataNode.attributes.get("name"));

    for (MetadataNode child : metadataNode.children) {
      if ("annotation".equals(child.name)) {
        settingMetadata.annotation(createAnnotationValue(child));
      }
    }
  }

  private void processClass(MetadataNode metadataNode) {
    String id = metadataNode.attributes.get("id");
    ClassMetadataBuilder classMetadata = this.classes.computeIfAbsent(id, key -> ClassMetadata.builder(Integer.parseInt(id)));
    classMetadata.name(metadataNode.attributes.get("name"));
    classMetadata.simpleType(Boolean.parseBoolean(metadataNode.attributes.getOrDefault("simpleType", "false")));

    for (MetadataNode child : metadataNode.children) {
      switch (child.name) {
        case "annotation":
          classMetadata.annotation(createAnnotationValue(child));
          break;
        case "field":
          classMetadata.field(createField(child));
          break;
      }
    }
  }

  private SettingValueBuilder createSettingValue(MetadataNode child) {
    String metadataId = child.attributes.get("class");

    SettingMetadataBuilder metadata = this.settings.computeIfAbsent(metadataId, key -> SettingMetadata.builder(Integer.parseInt(key)));
    SettingValueBuilder value = SettingValue.builder(metadata);
    for (Entry<String, String> entry : child.attributes.entrySet()) {
      String attributeName = entry.getKey();
      String attributeValue = entry.getValue();

      switch (attributeName) {
        case "name":
          value.name(attributeValue);
          break;
        case "defaultValue":
          value.defaultValue(attributeValue);
          break;
      }
    }

    return value;
  }

  private AnnotationValueBuilder createAnnotationValue(MetadataNode child) {

    String annotationTypeId = child.attributes.get("class");

    AnnotationMetadataBuilder metadata = this.annotations.computeIfAbsent(annotationTypeId, key -> AnnotationMetadata.builder(Integer.parseInt(key)));
    AnnotationValueBuilder value = AnnotationValue.builder(metadata);
    for (Entry<String, String> entry : child.attributes.entrySet()) {
      String attributeName = entry.getKey();
      String attributeValue = entry.getValue();

      if ("class".equals(attributeName)) {
        continue;
      }

      if (attributeValue.contains("-")) {
        value.value(attributeName, attributeValue.substring(0, attributeValue.lastIndexOf('-')));
      } else {
        value.value(attributeName, attributeValue);
      }
    }

    return value;
  }

  private FieldMetadataBuilder createField(MetadataNode child) {
    String name = child.attributes.get("name");
    String typeIdStr = child.attributes.get("class");
    String dimensionStr = child.attributes.get("dimension");

    int typeId = Integer.parseInt(typeIdStr);
    int dimension = dimensionStr != null ? Integer.parseInt(dimensionStr) : 0;

    ClassMetadataBuilder metadata = this.classes.computeIfAbsent(typeIdStr, key -> ClassMetadata.builder(typeId));
    FieldMetadataBuilder field = FieldMetadata.builder(metadata);
    field.name(name);
    field.constantPool(Boolean.parseBoolean(child.attributes.getOrDefault("constantPool", "false")));
    field.dimension(dimension);

    return field;
  }
}
