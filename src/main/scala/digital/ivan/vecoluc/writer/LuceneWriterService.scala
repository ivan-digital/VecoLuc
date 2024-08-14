package digital.ivan.vecoluc.writer

import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.MMapDirectory
import java.nio.file.Paths
import org.apache.lucene.document.{Document, KnnFloatVectorField, StoredField}

class LuceneWriterService(indexWriter: IndexWriter) {

  def this(indexPath: String) = {
    this(new IndexWriter(new MMapDirectory(Paths.get(indexPath)), new IndexWriterConfig()))
    indexWriter.commit()
  }

  def addDocument(metadata: Map[String, String], embeddings: Array[Float]): Unit = {
    val doc = new Document()

    metadata.foreach { case (name, value) =>
      doc.add(new StoredField(name, value))
    }

    doc.add(new KnnFloatVectorField("embeddings", embeddings))

    indexWriter.addDocument(doc)
  }

  def addDocuments(documents: Seq[(Map[String, String], Array[Float])]): Unit = {
    documents.foreach { case (metadata, embeddings) =>
      addDocument(metadata, embeddings)
    }
  }

  def commit(): Unit = {
    indexWriter.commit()
  }

  def close(): Unit = {
    indexWriter.close()
  }
}
