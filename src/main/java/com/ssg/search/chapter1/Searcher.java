package com.ssg.search.chapter1;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.queryParser.ParseException;

import java.io.File;
import java.io.IOException;

public class Searcher {
  public static void main(String[] args) throws IOException, ParseException {
    if(args.length != 2) {
      throw new IllegalArgumentException("Usage : java"
          + Searcher.class.getName()
          + " <index dir><query>");
    }

    // 색인 디렉토리를 알아 낸다
    String indexDir = args[0];

    // 커맨드 라인에 지정한 검색어를 뽑아 낸다
    String q = args[1];
    search(indexDir, q);
  }
  public static void search(String indexDir, String q) throws IOException, ParseException {
    Directory dir = FSDirectory.open(new File(indexDir));
    IndexSearcher is = new IndexSearcher(dir);

    QueryParser parser = new QueryParser(Version.LUCENE_30
        , "contents"
        , new StandardAnalyzer(Version.LUCENE_30));
    Query query = parser.parse(q);
    long start = System.currentTimeMillis();
    // 색인 내용 검색
    TopDocs hits = is.search(query, 10);
    long end = System.currentTimeMillis();

    // 검색 결과 관련 정보 표시
    System.err.println("Found " + hits.totalHits
        + " document(s) (in " + (end - start)
        + " milliseconds) that matched query '"
        + q + "':");
    for(ScoreDoc scoreDoc:hits.scoreDocs) {
      // 결과 문서 확보
      Document doc = is.doc(scoreDoc.doc);
      // IndexSearcher 닫기
      System.out.println(doc.get("fullpath"));
    }
    is.close();
  }
}
