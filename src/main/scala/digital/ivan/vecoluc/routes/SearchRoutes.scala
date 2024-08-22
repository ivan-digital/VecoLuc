package digital.ivan.vecoluc.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import digital.ivan.vecoluc.model.{ItemDoc, Response, SearchRequest, SearchResults}
import digital.ivan.vecoluc.reader.IndexSearchService
import io.circe.generic.auto._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.util.{Failure, Success}

class SearchRoutes(searchService: IndexSearchService)(implicit ec: ExecutionContext) extends LazyLogging {

  def routes: Route =
    path("search") {
      post {
        extractRequestContext { _ =>
          val startTime = System.nanoTime()
          try {
            entity(as[SearchRequest]) { searchRequest =>
              val searchFuture: Future[Seq[ItemDoc]] = Future {
                blocking {
                  searchService.searchByVector(searchRequest.query, searchRequest.topN)
                }
              }

              onComplete(searchFuture) {
                case Success(results) =>
                  val latency = (System.nanoTime() - startTime).nanos.toMillis
                  val searchResults = SearchResults(results, results.length)
                  complete(StatusCodes.OK, Response("success", latency, Some(searchResults)))
                case Failure(ex) =>
                  logger.error("Failed to execute search", ex)
                  complete(StatusCodes.InternalServerError, Response("error", 0, Some(ex.getMessage)))
              }
            }
          } catch {
            case ex: Exception =>
              logger.error("Failed to execute search", ex)
              complete(StatusCodes.InternalServerError, Response("error", 0, Some(ex.getMessage)))
          }
        }
      }
    }
}
