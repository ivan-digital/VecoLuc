package digital.ivan.vecoluc

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import digital.ivan.vecoluc.index.{IndexSearchService, ItemDoc, LuceneWriterService}

import scala.io.StdIn.readLine
import scala.util.{Failure, Success}
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.duration._
import akka.http.scaladsl.model.StatusCodes
import com.typesafe.scalalogging.LazyLogging

object VecolucApp extends App with LazyLogging {

  private val host = "0.0.0.0"
  private val port = 9000

  implicit val system: ActorSystem = ActorSystem(name = "vecoluc")
  import system.dispatcher

  case class Document(metadata: Map[String, String], embeddings: Array[Float])
  private case class Response[T](status: String, latencyMillis: Long, data: Option[T])
  private case class SearchResults(results: Seq[ItemDoc], count: Int)

  private val luceneWriteService = new LuceneWriterService("index")
  private val searchService = new IndexSearchService("index")

  private def route =
    path("index" / "doc") {
      post {
        extractRequestContext { ctx =>
          val startTime = System.nanoTime()
          try {
            entity(as[Document]) { doc =>
              luceneWriteService.addDocument(doc.metadata, doc.embeddings)
              val latency = (System.nanoTime() - startTime).nanos.toMillis
              complete(StatusCodes.OK, Response("success", latency, Option.empty[Unit]))
            }
          } catch {
            case ex: Exception =>
              logger.error("Failed to index document", ex)
              complete(StatusCodes.InternalServerError, Response("error", 0, Some(ex.getMessage)))
          }
        }
      }
    } ~ path("index" / "docs") {
      post {
        extractRequestContext { ctx =>
          val startTime = System.nanoTime()
          try {
            entity(as[Seq[Document]]) { documents =>
              luceneWriteService.addDocuments(documents.map(doc => (doc.metadata, doc.embeddings)))
              val latency = (System.nanoTime() - startTime).nanos.toMillis
              complete(StatusCodes.OK, Response("success", latency, Option.empty[Unit]))
            }
          } catch {
            case ex: Exception =>
              logger.error("Failed to index documents", ex)
              val latency = (System.nanoTime() - startTime).nanos.toMillis
              complete(StatusCodes.InternalServerError, Response("error", latency, Some(ex.getMessage)))
          }
        }
      }
    } ~ path("search") {
      get {
        extractRequestContext { ctx =>
          val startTime = System.nanoTime()
          try {
            parameters("query".as[String], "topN".as[Int]) { (query, topN) =>
              val vector = query.split(",").map(_.toFloat)
              val results = searchService.searchByVector(vector, topN)
              val latency = (System.nanoTime() - startTime).nanos.toMillis
              val searchResults = SearchResults(results, results.length)
              complete(StatusCodes.OK, Response("success", latency, Some(searchResults)))
            }
          } catch {
            case ex: Exception =>
              logger.error("Failed to execute search", ex)
              complete(StatusCodes.InternalServerError, Response("error", 0, Some(ex.getMessage)))
          }
        }
      }
    }

  private val binding = Http().newServerAt(host, port).bind(route)
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
