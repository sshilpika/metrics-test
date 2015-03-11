package luc.metrics.dashboard

/**
 * Created by sshilpika on 3/8/15.
 */


import spray.routing.directives.OnCompleteFutureMagnet
import scala.util.{Success, Try}
import scala.concurrent.ExecutionContext.Implicits._
import akka.actor._
import spray.httpx.encoding.Deflate
import spray.routing.authentication.BasicAuth
import scala.util.{Try, Failure, Success}
import spray.routing._
import spray.http._
import MediaTypes._

trait MetricDashboardService extends HttpService {

  // is the below code required
  /*def onCompleteWithRepoErrorHandler[T](m: OnCompleteFutureMagnet[T])(body: PartialFunction[Try[T], Route]) =
    onComplete(m)(body orElse repoErrorHandler)

    def repoErrorHandler[T]: PartialFunction[Try[T], Route] = {
    case Success(_) => complete(StatusCodes.NotFound)
    case _ => complete(StatusCodes.InternalServerError)
    }*/

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    }~
      path("commits") {
        get{

          parameters('user, 'repo) { (user, repo) =>
            onComplete(Program.testing(user,repo,"commits")) { complete(_) }
          }
        }
      }~
      path("issues") {
        get{

          parameters('user, 'repo) { (user, repo) =>
            onComplete(Program.testing(user,repo,"issues")) { complete(_) }
          }

        }
      }~
      path("languages") {
        get{

          parameters('user, 'repo) { (user, repo) =>
            onComplete(Program.testing(user,repo,"languages")) { complete(_) }
          }

        }
      }

  // TODO Authentication

  val route = {
    path("orders") {
      authenticate(BasicAuth(realm = "admin area")) { user =>
        get {
          //cache(simpleCache) {
            encodeResponse(Deflate) {
              complete {
                // marshal custom object with in-scope marshaller
                <html>
                  <body>
                    <h1>DOne using AUth</h1>
                  </body>
                </html>
             // }
            }
          }
        }
      }
    }
  }

}


class MyServiceActor extends Actor with MetricDashboardService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}
