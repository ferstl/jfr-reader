package com.github.ferstl.jfrreader.parser.metadata;

public abstract class BasicMetadata {

  // * includes all primitive types * //
  // !!! Field has no ID !!! //
  public final int id;
  public String name;

  public BasicMetadata(int id) {
    this.id = id;
  }

}
