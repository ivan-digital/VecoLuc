package digital.ivan.vecoluc

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.LazyLogging
import digital.ivan.vecoluc.reader.{IndexSearchService, LuceneSearcher}
import digital.ivan.vecoluc.routes.{IndexRoutes, SearchRoutes}
import digital.ivan.vecoluc.writer.LuceneWriterService

import scala.io.StdIn.readLine
import scala.util.{Failure, Success}

object VecolucApp extends App with LazyLogging {

  private val host = "0.0.0.0"
  private val port = 9000
  private val indexName = "index"

  implicit val system: ActorSystem = ActorSystem(name = "vecoluc")

  import system.dispatcher

  private val luceneWriteService = new LuceneWriterService(indexName)
  private val luceneIndexSearcher = LuceneSearcher(indexName)
  private val searchService = new IndexSearchService(luceneIndexSearcher)
  private val indexRoutes = new IndexRoutes(luceneWriteService)
  private val searchRoutes = new SearchRoutes(searchService)
  private val binding = Http().newServerAt(host, port).bind(route)

  private def route = concat(
    indexRoutes.routes,
    searchRoutes.routes
  )
  binding.onComplete {
    case Success(_) =>
      logger.info("VecoLuc: Listening for incoming connections!")
    case Failure(error) =>
      logger.error("VecoLuc failed to start", error)
  }

  sys.addShutdownHook {
    try {
      luceneWriteService.close()
      logger.info("Lucene writer closed successfully.")
    } catch {
      case ex: Exception =>
        logger.error("Failed to close Lucene writer", ex)
    }
  }

  readLine()
}
