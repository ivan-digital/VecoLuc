package digital.ivan.vecoluc.index
import org.apache.lucene.document.{Document, KnnFloatVectorField, StoredField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.MMapDirectory

import java.nio.file.Paths

class LuceneWriterService(indexPath: String) {

  private val directory = new MMapDirectory(Paths.get(indexPath))
  private val config = new IndexWriterConfig()
  private val writer = new IndexWriter(directory, config)

  def addDocument(metadata: Map[String, String], embeddings: Array[Float]): Unit = {
    val doc = new Document()

    metadata.foreach { case (name, value) =>
      doc.add(new StoredField(name, value))
    }

    doc.add(new KnnFloatVectorField("embeddings", embeddings))

    writer.addDocument(doc)
  }

  def addDocuments(documents: Seq[(Map[String, String], Array[Float])]): Unit = {
    documents.foreach { case (metadata, embeddings) =>
      addDocument(metadata, embeddings)
    }
  }

  def close(): Unit = {
    writer.close()
  }
}
