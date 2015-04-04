package luc.metrics.dashboard

/**
 * Created by sshilpika on 3/8/15.
 */


import java.net.URI

import _root_.scredis.Redis
import akka.util.Timeout
import spray.httpx.SprayJsonSupport
import spray.routing.directives.OnCompleteFutureMagnet
import scala.util._
import scala.concurrent.ExecutionContext.Implicits._
import akka.actor._
import spray.httpx.encoding.Deflate
import spray.routing.authentication.BasicAuth
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json._
import scala.concurrent.Future

import spray.can.Http
import akka.io.IO
import akka.pattern.ask
import spray.http._
import HttpMethods._
import scala.concurrent.duration._

import spray.httpx.marshalling._
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport._
import org.apache.commons.codec.binary.Base64
import spray.json.lenses.JsonLenses._

case class githubRepoContent( name:String, path:String, size:Int ,contents:String)
case class Content(content:String)
object RepoContentJsonSupport extends DefaultJsonProtocol with SprayJsonSupport{
  implicit val contentFormat = jsonFormat4(githubRepoContent)
  //implicit val contents = jsonFormat(Content,"content")
}


case class gitH(content:String)


object gitFor extends DefaultJsonProtocol{
  implicit val con = jsonFormat(gitH,"content")

  def write(c: gitH) =
    JsArray(JsString(c.content))
}

trait MetricDashboardService extends HttpService {

  // is the below code required
  /*def onCompleteWithRepoErrorHandler[T](m: OnCompleteFutureMagnet[T])(body: PartialFunction[Try[T], Route]) =
    onComplete(m)(body orElse repoErrorHandler)

    def repoErrorHandler[T]: PartialFunction[Try[T], Route] = {
    case Success(_) => complete(StatusCodes.NotFound)
    case _ => complete(StatusCodes.InternalServerError)
    }*/
  //implicit val sprayCounterFormat = jsonFormat3(Counter.apply)
  //case class httpResponse(future: Future[String])

  //object MyJsonProtocol extends DefaultJsonProtocol {
    //implicit val colorFormat = jsonFormat4(httpResponse)
  //}


  import spray.httpx.RequestBuilding._
  def githubCall(user: String, repo: String, responseType:String): Future[HttpResponse] =  {
    implicit val actor = ActorSystem("githubCall")
    implicit val timeout = Timeout(5.seconds)
    val response2: Future[HttpResponse] =
      (IO(Http) ? Get("https://api.github.com/repos/"+user+"/"+repo+"/"+responseType)).mapTo[HttpResponse]

    response2
  }


  def githubCallToContents(user: String, repo: String, filePath:String): Future[String] =  {
    implicit val actor = ActorSystem("githubCallForContent")
    implicit val timeout = Timeout(15.seconds)
    val contents = Future{
      //println("https://api.github.com/repos/"+user+"/"+repo+"/contents/"+filePath)
      (IO(Http) ? Get("https://api.github.com/repos/"+user+"/"+repo+"/contents/"+filePath)).mapTo[HttpResponse]
    }
    import RepoContentJsonSupport._
    /*val c = contents.flatMap(x => {x.flatMap(x1 => {
        val result = x1.toString.parseJson.asJsObject.getFields("name","size","path","contents") match{
          case Seq(JsString(name), JsString(path), JsNumber(size), JsString(contents1)) =>
            new githubRepoContent(name, path, size.toInt, contents1)
        }

        Future{new String(Base64.decodeBase64(result.contents),"UTF-8")}
      })})*/

    val res2 = contents.flatMap(x => {x.flatMap(x1 => {

      //val str = EntityUtils.toString(x1.entity)
     // val jsonJsVal = JsonParser(x1.entity.data.toString)//as[JsValue].extract[Content]('content / *)
      /*import gitFor._
      val test = x1.entity.toString.toJson
      val allAuthors = 'content
      val authorNames = test.extract[String](allAuthors)
      //Future{jsonJsVal.toString}
      Future{authorNames}*/
      Future{x1.entity.toString}
     // val jsonCode = jsonJsVal.as[gitH]
      //Future{new String(Base64.decodeBase64(jsonCode.toJson.convertTo[gitH].content),"UTF-8")}
      //Future{new String(Base64.decodeBase64(authorname),"UTF-8")}
      })
    })

    val url = new URI(Properties.envOrElse("REDISCLOUD_URL", "redis://localhost:6379"))
    val client = new Redis(url.getHost, url.getPort)
    for (userInfo <- Option(url.getUserInfo)) {
      val secret = userInfo.split(':')(1)
      client.auth(secret)
    }
    val key = "hello"
    val value = "world"
    client.set(key, value)
    //val s = client.hGetAll(url.getHost)
    val s:Future[Option[String]] = client.get(key)
    //contents flatMap {x => x}
    val res =for {
      a <- s
      if(a.isDefined)
    }yield a

    res2

  }

  val completeWithUnmatchedPath =
    unmatchedPath { p =>
      complete(p.toString)
    }
  val myRoute =
    path("test") {
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
    }~ pathEndOrSingleSlash{

        complete("/foo test")


    }~ pathPrefix("prefixTest"){
        pathEnd {
          complete("/foo1111 test")
        }~path("path2"){
          complete("Prefix path2")
        }

    }~ pathPrefixTest("PPT1" | "PPT2"){
      pathPrefix("PPT1"){
        completeWithUnmatchedPath
      }~ pathPrefix("PPT2"){
        completeWithUnmatchedPath
      }
    }~ path("Seg"/Segment) {

      s =>
        complete(if (s.equals("segment")) "even ball" else "odd ball")
    }~
      path("commits") {
        get{

          parameters('user, 'repo) { (user, repo) =>
            onComplete (githubCall(user,repo,"commits")){
              case Success(value) => respondWithMediaType(`application/json`) {
                complete(value)
              }
              //case Failure(ex)    => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            }
          }
        }
      }~ path("issues") {
      //redirect("https://api.github.com/repos/"+user+"/"+repo+"/"+responseType)

        get{

          parameters('user, 'repo) { (user, repo) =>
            redirect("https://api.github.com/repos/"+user+"/"+repo+"/issues",StatusCodes.TemporaryRedirect)//PermanentRedirect)
            //onComplete(githubCall(user,repo,"issues")) {complete(_)}
          }

        }
      }~
        path("languages") {
          get{

            parameters('user, 'repo) { (user, repo) =>
              onComplete(githubCall(user,repo,"languages")) {
                case Success(value) => respondWithMediaType(`application/json`) {
                  complete(value)
                }
               // case Failure(ex)    => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
              }
            }

          }
        }~
      path("loc") {
        get{

          //parameters('user, 'repo) { (user, repo) =>
            onComplete(githubCallToContents("sshilpika","metrics-test","src/main/scala/Boot.scala")) {
              case Success(value) =>  //respondWithMediaType(`application/json`)  {
                complete(value)
              //}
              //case Failure(ex)    => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
             // complete{

              /*(x:Future[DefaultJsonProtocol]) => {
                  x onSuccess {
                    case _ => _
                  }


                } *///}
            }}
          //}


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
  //import context.dispatcher
  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}
