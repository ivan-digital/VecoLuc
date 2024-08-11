package digital.ivan.vecoluc.index

import org.apache.lucene.index.{DirectoryReader, IndexReader, LeafReader, StoredFields}
import org.apache.lucene.search.{IndexSearcher, KnnFloatVectorQuery}
import org.apache.lucene.store.FSDirectory

import java.nio.file.Paths
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.IterableHasAsScala
import com.typesafe.scalalogging.LazyLogging
import org.apache.lucene.document.Document

class IndexSearchService(indexPath: String) extends LazyLogging {

  @volatile private var indexSearcher: Option[IndexSearcher] = None

  private def initSearcher(): Unit = synchronized {
    if (indexSearcher.isEmpty) {
      logger.info(s"Initializing IndexSearcher for index at $indexPath")
      val indexDirectory = FSDirectory.open(Paths.get(indexPath))
      val reader = DirectoryReader.open(indexDirectory)
      indexSearcher = Some(new IndexSearcher(reader))
      logger.info("IndexSearcher initialized successfully")
    }
  }

  def searchByVector(vector: Array[Float], topN: Int): Seq[ItemDoc] = {
    if (indexSearcher.isEmpty) {
      initSearcher()
    }

    val query = new KnnFloatVectorQuery("embeddings", vector, topN)
    val topDocs = indexSearcher.get.search(query, topN)

    val documents = ListBuffer[ItemDoc]()
    for (scoreDoc <- topDocs.scoreDocs) {
      val doc = getDocument(scoreDoc.doc)
      documents += documentToItemDoc(doc, scoreDoc.score)
    }

    documents.toSeq
  }

  private def getDocument(docId: Int): Map[String, String] = {
    val allFields = ListBuffer[(String, String)]()

    val indexReader: IndexReader = indexSearcher.get.getIndexReader
    val document: Document = indexReader.document(docId)

    document.getFields.asScala.foreach { field =>
      allFields += field.name() -> document.get(field.name())
    }

    allFields.toMap
  }

  private def documentToItemDoc(fields: Map[String, String], score: Float): ItemDoc = {
    ItemDoc(fields, score)
  }
}

case class ItemDoc(fields: Map[String, String], score: Float)
