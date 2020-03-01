package com.github.ferstl.jfrreader.parser.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataNode {

  public String name;
  public Map<String, String> attributes = new HashMap<>();
  public List<MetadataNode> children = new ArrayList<>();

  public void accept(MetadataNodeVisitor visitor) {
    visitor.visit(this);
    for (MetadataNode child : this.children) {
      child.accept(visitor);
    }
  }
}
