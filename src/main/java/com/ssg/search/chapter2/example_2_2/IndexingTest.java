package com.ssg.search.chapter2.example_2_2;

import com.ssg.search.common.TestUtil;
import junit.framework.TestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

public class IndexingTest extends TestCase {
  protected String[] ids = {"1", "2"};
  protected String[] unindexed = {"Netherlands", "Italy"};
  protected String[] unstored = {"Amsterdam has lots of bridges", "Venice has lots of canals"};
  protected String[] text = {"Amsterdam", "Venice"};
  // IndexWriter는 먼저 Directory 공간 안에 기존 색인이 있는지 없는지 확인하고
  // 없다면 빈 색인을 새로 만든다
  private Directory directory;

  protected void setUp() throws Exception {
    directory = new RAMDirectory();
    // IndexWriter 객체 생성
    IndexWriter writer = getWriter();
    for (int i = 0; i < ids.length; i++) {
      Document doc = new Document();
      doc.add(new Field("id", ids[i],
          Field.Store.YES,
          Field.Index.NOT_ANALYZED));
      doc.add(new Field("country", unindexed[i],
          Field.Store.YES,
          Field.Index.NO));
      doc.add(new Field("contents", unstored[i],
          Field.Store.NO,
          Field.Index.ANALYZED));
      doc.add(new Field("city", text[i],
          Field.Store.YES,
          Field.Index.ANALYZED));
      writer.addDocument(doc);
    }
    // IndexWriter를 닫으며 자동으로 변경 사항을 저장 공간에 반영
    writer.close();
  }

  private IndexWriter getWriter() throws IOException {
    return new IndexWriter(directory, new WhitespaceAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
  }

  private int getHitCount(String fieldName, String searchString) throws IOException {
    IndexSearcher searcher = new IndexSearcher(directory);
    Term t = new Term(fieldName, searchString);
    // 간단한 단일 텀 질의 생성
    Query query = new TermQuery(t);
    int hitCount = TestUtil.hitCount(searcher, query);
    return hitCount;
  }

  public void testIndexWriter() throws IOException {
    IndexWriter writer = getWriter();
    // IndexWriter 문서 개수 확인
    assertEquals(ids.length, writer.numDocs());
    writer.close();
  }

  public void testIndexReader() throws IOException {
    IndexReader reader = IndexReader.open(directory);
    // IndexReader 문서 개수 확인
    assertEquals(ids.length, reader.maxDoc());
    assertEquals(ids.length, reader.numDocs());
    reader.clone();
  }

  public void testDeleteBeforeOptimize() throws IOException {
    IndexWriter writer = getWriter();
    assertEquals(2, writer.numDocs());
    // 첫 번째 문서 삭제
    writer.deleteDocuments(new Term("id", "1"));
    writer.commit();
    // 색인에 삭제된 문서가 있는지 검증
    assertTrue(writer.hasDeletions());
    // 색인된 문서 1건, 삭제된 문서 1건 확인
    assertEquals(2, writer.maxDoc());
    assertEquals(1, writer.numDocs());
    writer.close();
  }

  public void testDeleteAfterOptimize() throws IOException {
    IndexWriter writer = getWriter();
    assertEquals(2, writer.numDocs());
    writer.deleteDocuments(new Term("id", "1"));
    // 삭제된 문서를 실제로 제거하게 최적화 실행
    writer.optimize();                //3
    writer.commit();
    assertFalse(writer.hasDeletions());
    // 색인된 문서 1건, 삭제된 문서는 없다고 확인
    assertEquals(1, writer.maxDoc());  //C
    assertEquals(1, writer.numDocs()); //C
    writer.close();
  }

}
