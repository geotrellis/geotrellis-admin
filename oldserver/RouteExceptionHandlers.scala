package geotrellis.admin.server

import spray.http.StatusCodes
import spray.routing.{ExceptionHandler, HttpService}
import spray.util.LoggingContext

trait RouteExceptionHandlers extends HttpService {
  
  implicit def exceptionHandler(implicit log: LoggingContext) = ExceptionHandler {
    case e: Exception => complete(StatusCodes.NotFound, e.getMessage())        
  }    

}
