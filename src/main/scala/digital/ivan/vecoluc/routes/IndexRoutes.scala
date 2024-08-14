package digital.ivan.vecoluc.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import digital.ivan.vecoluc.model.{Document, Response}
import digital.ivan.vecoluc.writer.LuceneWriterService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.duration._

class IndexRoutes(luceneWriteService: LuceneWriterService) extends LazyLogging {

  def routes: Route =
    pathPrefix("index") {
      concat(
        path("docs") {
          post {
            extractRequestContext { _ =>
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
        },
        path("commit") {
          post {
            extractRequestContext { _ =>
              val startTime = System.nanoTime()
              try {
                luceneWriteService.commit()
                val latency = (System.nanoTime() - startTime).nanos.toMillis
                complete(StatusCodes.OK, Response("success", latency, Option.empty[Unit]))
              } catch {
                case ex: Exception =>
                  logger.error("Failed to commit changes", ex)
                  val latency = (System.nanoTime() - startTime).nanos.toMillis
                  complete(StatusCodes.InternalServerError, Response("error", latency, Some(ex.getMessage)))
              }
            }
          }
        }
      )
    }
}

