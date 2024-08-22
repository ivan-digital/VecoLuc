package digital.ivan.vecoluc.reader

import digital.ivan.vecoluc.model.ItemDoc
import org.apache.lucene.search.KnnFloatVectorQuery

class IndexSearchService(searchService: LuceneSearcher) {

  def searchByVector(vector: Array[Float], topN: Int): Seq[ItemDoc] = {
    val query = new KnnFloatVectorQuery("embeddings", vector, topN)
    val scoreDocs = searchService.search(query, topN)

    scoreDocs
      .map { scoreDoc =>
        documentToItemDoc(searchService.getDocument(scoreDoc.doc), scoreDoc.score)
      }.toSeq
  }

  private def documentToItemDoc(fields: Map[String, String], score: Float): ItemDoc = {
    ItemDoc(fields, score)
  }
}