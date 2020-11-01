package com.ssg.search.chapter1.example_1_2;

public class Searcher {
  public static void main(String[] args) {
    if(args.length != 2) {
      throw new IllegalArgumentException("Usage : java"
          + Searcher.class.getName()
          + " <index dir><query>");
    }

    String indexDir = args[0];
    String q = args[1];
    search(indexDir, q);

  }
}
