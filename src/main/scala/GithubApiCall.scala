/**
 * Created by sshilpika on 3/9/15.
 */
// Akka Actor system
package luc.metrics.dashboard

import akka.actor.ActorSystem
import spray.http.{ HttpRequest, HttpResponse }
import spray.client.pipelining.{ Get, sendReceive }
import scala.concurrent.Future
import scala.util.{ Success, Failure }

// http://stackoverflow.com/questions/20593280/error-with-akka
// add dependency typesafe-config.jar

// Need to wrap in a package to get application supervisor actor
// "you need to provide exactly one argument: the class of the application supervisor actor"


// trait with single function to make a GET request
trait WebClient {
  def get(url: String): Future[String]
}

// implementation of WebClient trait
class SprayWebClient(implicit system: ActorSystem) extends WebClient {
  import system.dispatcher

  // create a function from HttpRequest to a Future of HttpResponse
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  // create a function to send a GET request and receive a string response
  def get(url: String): Future[String] = {
    val futureResponse = pipeline(Get(url))
    futureResponse.map(_.entity.asString)
  }
}

object Program {

  implicit val system = ActorSystem ()
  import system.dispatcher

  // execution context for futures
  def testing(user:String,repo:String,requestType:String): Future[String] ={

    // bring the actor system in scope

    // create the client
    val webClient = new SprayWebClient () (system)

    // send GET request with absolute URI
    val futureResponse = webClient.get ("https://api.github.com/repos/"+user+"/"+repo+"/"+requestType)

    futureResponse
    //synchronized { futureResponse }
  }
}

