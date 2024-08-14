package digital.ivan.vecoluc.model

case class Document(metadata: Map[String, String], embeddings: Array[Float])
case class Response[T](status: String, latencyMillis: Long, data: Option[T])
case class SearchResults(results: Seq[ItemDoc], count: Int)
case class SearchRequest(query: Array[Float], topN: Int)
