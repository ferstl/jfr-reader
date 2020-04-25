package com.github.ferstl.jfrreader.parser.metadata;

import java.util.HashMap;
import java.util.Map;
import com.github.ferstl.jfrreader.parser.metadata.ClassInstance.ClassInstanceBuilder;

public class ConstantPool {

  private final Map<Integer, Map<Long, ClassInstanceBuilder>> constantsByClass;

  public ConstantPool() {
    this.constantsByClass = new HashMap<>();
  }

  public ClassInstanceBuilder computeClassInstance(int classId, long index, ClassMetadata classMetadata) {
    Map<Long, ClassInstanceBuilder> constants = this.constantsByClass.computeIfAbsent(classId, key -> new HashMap<>());
    return constants.computeIfAbsent(index, key -> ClassInstance.builder(classMetadata));
  }
}
