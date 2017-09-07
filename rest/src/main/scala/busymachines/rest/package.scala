package busymachines

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
package object rest {

  import akka.http.scaladsl
  import akka.http.scaladsl.settings
  import akka.http.scaladsl.server
  import akka.http.scaladsl.model

  val Http: scaladsl.Http.type = scaladsl.Http

  type HttpRequest = model.HttpRequest
  val HttpRequest: model.HttpRequest.type = model.HttpRequest

  type HttpResponse = model.HttpResponse
  val HttpResponse: model.HttpResponse.type = model.HttpResponse

  type Route = server.Route
  val Route: server.Route.type = server.Route

  type Directives = server.Directives
  val Directives: server.Directives.type = server.Directives

  type ExceptionHandler = server.ExceptionHandler
  val ExceptionHandler: server.ExceptionHandler.type = server.ExceptionHandler

  type RejectionHandler = server.RejectionHandler
  val RejectionHandler: server.RejectionHandler.type = server.RejectionHandler

  type StatusCode = model.StatusCode
  val StatusCode: model.StatusCode.type = model.StatusCode
  val StatusCodes: model.StatusCodes.type = model.StatusCodes

  type RoutingSettings = settings.RoutingSettings
  val RoutingSettings: settings.RoutingSettings.type = settings.RoutingSettings

  import de.heikoseeberger.akkahttpcirce

  type JsonSupport = akkahttpcirce.FailFastCirceSupport
  val JsonSupport: akkahttpcirce.FailFastCirceSupport.type = akkahttpcirce.FailFastCirceSupport

  type ErrorAccumulatingJsonSupport = akkahttpcirce.ErrorAccumulatingCirceSupport
  val ErrorAccumulatingJsonSupport: akkahttpcirce.ErrorAccumulatingCirceSupport.type = akkahttpcirce.ErrorAccumulatingCirceSupport
}
