package digital.ivan.vecoluc.reader

import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.{IndexSearcher, KnnFloatVectorQuery, ScoreDoc, TopDocs, TotalHits}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar

class LuceneSearcherTest extends AnyFlatSpec with Matchers with MockitoSugar {

  "search" should "return a sequence of ScoreDocs when indexSearcher is present" in {
    val indexSearcher = mock[IndexSearcher]
    val indexReader = mock[IndexReader]
    when(indexSearcher.getIndexReader).thenReturn(indexReader)

    val luceneSearcher = new LuceneSearcher(indexSearcher)

    val scoreDocs = Array(new ScoreDoc(1, 1.0f), new ScoreDoc(2, 0.9f))
    val topDocs = new TopDocs(new TotalHits(2, TotalHits.Relation.EQUAL_TO), scoreDocs)

    when(indexSearcher.search(any[KnnFloatVectorQuery], any[Int])).thenReturn(topDocs)

    val result = luceneSearcher.search(new KnnFloatVectorQuery("embeddings", Array(0.1f, 0.2f, 0.3f), 2), 2)

    result should have size 2
    result.head.doc should be(1)
    result.head.score should be(1.0f)
    result(1).doc should be(2)
    result(1).score should be(0.9f)
  }

  "getDocument" should "return a map of field names to values when a document is retrieved" in {
    val indexSearcher = mock[IndexSearcher]
    val indexReader = mock[IndexReader]
    when(indexSearcher.getIndexReader).thenReturn(indexReader)

    val luceneSearcher = new LuceneSearcher(indexSearcher)

    val doc = new org.apache.lucene.document.Document()
    doc.add(new org.apache.lucene.document.TextField("field1", "value1", org.apache.lucene.document.Field.Store.YES))
    doc.add(new org.apache.lucene.document.TextField("field2", "value2", org.apache.lucene.document.Field.Store.YES))

    when(indexReader.document(1)).thenReturn(doc)

    val result = luceneSearcher.getDocument(1)

    result should contain("field1" -> "value1")
    result should contain("field2" -> "value2")
  }
}
