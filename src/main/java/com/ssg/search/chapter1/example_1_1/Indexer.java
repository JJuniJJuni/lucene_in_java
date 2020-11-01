package com.ssg.search.chapter1.example_1_1;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

public class Indexer {
  public static void main(String[] args) throws Exception {
    if(args.length != 2) {
      throw new IllegalArgumentException("Usage:java + "
          + Indexer.class.getName()
          + "<index dir><data dir>");
    }
    // 지정한 디렉토리에 색인 생성
    String indexDir = args[0];

    // 지정한 디렉토리에 담긴 txt 파일
    String dataDir = args[1];
    long start = System.currentTimeMillis();
    Indexer indexer = new Indexer(indexDir);
    int numIndexed;

    try {
      numIndexed = indexer.index(dataDir, new TextFilesFilter());
    } finally {
      indexer.close();
    }
    long end = System.currentTimeMillis();
    System.out.println("Indexing " + numIndexed + " files took "
        + (end - start) + " millseconds");


  }
  private IndexWriter writer;

  // 루씬의 IndexWriter 생성
  public Indexer (String indexDir) throws IOException {
    Directory dir = FSDirectory.open(new File(indexDir));
    this.writer = new IndexWriter(dir
        , new StandardAnalyzer(Version.LUCENE_30)
        , true
        , IndexWriter.MaxFieldLength.UNLIMITED);

  }

  // IndexWriter 닫음
  public void close() throws  IOException {
    this.writer.close();
  }
  public int index(String dataDir, FileFilter filter) throws Exception {
    File[] files = new File(dataDir).listFiles();
    for(File f: files) {
      if(!f.isDirectory()
          && !f.isHidden()
          && f.exists()
          && f.canRead()
          && (filter == null) || filter.accept(f)) {
        indexFile(f);
      }
    }
    // 색인된 문서 건수 리턴
    return this.writer.numDocs();
  }
  private static class TextFilesFilter implements  FileFilter {
    public boolean accept(File path) {
      // FileFilter를 사용해 색인할 txt 파일만 추려낸다.
      return path.getName().toLowerCase().endsWith(".txt");
    }
  }
  protected Document getDocument(File f) throws Exception {
    Document doc = new Document();
    // 파일의 내용 추가
    doc.add(new Field("contents", new FileReader(f)));
    // 파일 이름 추가
    doc.add(new Field("filename", f.getName()
        , Field.Store.YES, Field.Index.NOT_ANALYZED));
    // 파일의 전체 경로 추가
    doc.add(new Field("fullpath", f.getCanonicalPath()
        , Field.Store.YES, Field.Index.NOT_ANALYZED));
    return doc;
  }
  private void indexFile(File f) throws Exception {
    System.out.println("Indexing " + f.getCanonicalPath());
    Document doc = getDocument(f);
    //파일 색인에 문서 추가
    this.writer.addDocument(doc);
  }
}
