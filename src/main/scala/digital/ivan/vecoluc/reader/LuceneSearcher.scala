package digital.ivan.vecoluc.reader

import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.{IndexSearcher, KnnFloatVectorQuery, ScoreDoc}
import org.apache.lucene.store.FSDirectory

import java.lang.Runtime.getRuntime
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors.newFixedThreadPool
import scala.jdk.CollectionConverters.CollectionHasAsScala

class LuceneSearcher(indexSearcher: IndexSearcher) {

  def search(query: KnnFloatVectorQuery, topN: Int): Array[ScoreDoc] = {
    indexSearcher.search(query, topN).scoreDocs
  }

  def getDocument(docId: Int): Map[String, String] = {
    val document = indexSearcher.getIndexReader.document(docId)
    document.getFields.asScala.map { field =>
      field.name() -> document.get(field.name())
    }.toMap
  }
}

object LuceneSearcher {
  def apply(indexPath: String): LuceneSearcher = {
    val indexDirectory = FSDirectory.open(Paths.get(indexPath))
    val reader = DirectoryReader.open(indexDirectory)
    val threadPool: ExecutorService = newFixedThreadPool(getRuntime.availableProcessors())
    val indexSearcher = new IndexSearcher(reader, threadPool)
    new LuceneSearcher(indexSearcher)
  }
}