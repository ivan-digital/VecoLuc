package digital.ivan.vecoluc.reader

import digital.ivan.vecoluc.writer.LuceneWriterService
import org.apache.lucene.document.{Document, KnnFloatVectorField, StoredField}
import org.apache.lucene.index.IndexWriter
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

class LuceneWriterServiceTest extends AnyFlatSpec with Matchers with MockitoSugar {

  "addDocument" should "add a document with the correct metadata and embeddings" in {
    val indexWriter = mock[IndexWriter]
    val luceneWriterService = new LuceneWriterService(indexWriter)

    val metadata = Map("field1" -> "value1", "field2" -> "value2")
    val embeddings = Array(0.1f, 0.2f, 0.3f)

    luceneWriterService.addDocument(metadata, embeddings)

    val documentCaptor = ArgumentCaptor.forClass(classOf[Document])
    verify(indexWriter).addDocument(documentCaptor.capture())

    val capturedDoc = documentCaptor.getValue

    capturedDoc.get("field1") mustBe "value1"
    capturedDoc.get("field2") mustBe "value2"

    val embeddingField = capturedDoc.getField("embeddings").asInstanceOf[KnnFloatVectorField]
    embeddingField.name() mustBe "embeddings"
  }

  "addDocuments" should "add multiple documents" in {
    val indexWriter = mock[IndexWriter]
    val luceneWriterService = new LuceneWriterService(indexWriter)

    val documents = Seq(
      (Map("field1" -> "value1"), Array(0.1f, 0.2f)),
      (Map("field2" -> "value2"), Array(0.3f, 0.4f))
    )

    luceneWriterService.addDocuments(documents)

    val documentCaptor = ArgumentCaptor.forClass(classOf[Document])
    verify(indexWriter, times(2)).addDocument(documentCaptor.capture())

    val capturedDocs = documentCaptor.getAllValues

    capturedDocs.get(0).get("field1") mustBe "value1"
    val firstEmbeddingField = capturedDocs.get(0).getField("embeddings").asInstanceOf[KnnFloatVectorField]
    firstEmbeddingField.name() mustBe "embeddings"

    capturedDocs.get(1).get("field2") mustBe "value2"
    val secondEmbeddingField = capturedDocs.get(1).getField("embeddings").asInstanceOf[KnnFloatVectorField]
    secondEmbeddingField.name() mustBe "embeddings"
  }

  "close" should "close the IndexWriter" in {
    val indexWriter = mock[IndexWriter]
    val luceneWriterService = new LuceneWriterService(indexWriter)

    luceneWriterService.close()

    verify(indexWriter).close()
  }
}
