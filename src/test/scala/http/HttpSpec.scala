package edu.luc.etl.cs313.scala.metrics.dashboard.service

import org.mindrot.jbcrypt.BCrypt
import org.specs2.matcher._//JsonMatchers
import org.specs2.mutable._
import dispatch._, Defaults._
import scala.util.{Success, Failure}

class HttpSpec extends Specification with JsonMatchers with XmlMatchers {

  val serviceRoot = host("private-56d87-metricsdashboardapi.apiary-mock.com")
  val homeDir = System.getProperty("user.home")
  val accessToken = scala.io.Source.fromFile(homeDir + "/githubAccessToken").getLines.next()
  val bcryptHash = scala.io.Source.fromFile(homeDir + "/bcryptHash").getLines.next()

  "The metrics dashboard service with respect to authentication" should {

    "return success for OauthAccessTokenAuthentication" in {
      val request = serviceRoot / "users" / "username"
      println("!!!!!!!!! "+request)
      val myPost = request.POST
      myPost.addParameter("client_id", "xxxx")
      myPost.addParameter("client_secret", "yyyy")
      println(myPost.toString+" MYPOST")
      val response = Http(myPost)
      val status = response().getStatusCode
      println("TESTTTTTT "+status)
      status mustEqual(200)

    }

   "retrieve an existing counter" in {
      //val hashed = BCrypt.hashpw(password, BCrypt.gensalt());

      val request = serviceRoot / "login" / "oauth" / accessToken
      println("TEST222")
      val response = Http(request OK as.xml.Elem)
      println("XMLLLLLL "+response)
      val oauthResponse = response()
      println("XML1 "+oauthResponse)
            println("ATTR "+oauthResponse(0).descendant(8))
      val test = oauthResponse(0).descendant(8).toString().trim
      println("TEST "+test)
      println(bcryptHash)
      println(BCrypt.checkpw(test, bcryptHash)+" PASS")
      BCrypt.checkpw(test, bcryptHash) mustEqual true

    }

    /*"delete an existing counter" in {
      todo
    }*/
  }

  "The metrics dashboard service with respect to repositories" should {

    "return success for OauthAccessTokenAuthentication" in {
      val request = serviceRoot / "repos" /  "provider" / "owner" / "reponame" / "commits"
      println("!!!!!!!!! "+request)
      val response = Http(request OK as.String)
      val repoResponse = response()
      repoResponse must / ("commit") */("author") /("name" -> "author")
      repoResponse must / ("commit") /# 2 /("message" -> "Initial commit")
      repoResponse must */("email" -> "author@luc.edu")


    }

    "retrieve an existing counter" in {

      val request = serviceRoot / "repos" / "django" / "django" / "commits"
      val myGet = request.GET
      myGet.addParameter("since","2015-02-14T22:03:01Z")
      val response = Http(request OK as.String)
      val repoResponse = response()
      repoResponse must /("commit") /# 2 /("message" -> "Added missing return value to DurationField.prepare_value()")

       repoResponse must /("commit") */("committer") */("name" -> "Tim Graham")
      repoResponse must / ("commit") /# 1 */("email" -> "timograham@gmail.com")
      //repoResponse must / ("commit") /# 2 /("message" -> "\\w+")

    }


  }

  "The metrics dashboard service with respect to Issues" should {

   "return success for Issue Search" in {
      val request = serviceRoot / "search" /  "issues"
      println("!!!!!!!!! "+request)
      val myGet = request.GET
      myGet.addParameter("p","counter+language:scala+user:LoyolaChicagoCode+state:open")
      val response = Http(request OK as.String)
      val repoResponse = response()
      repoResponse must */ ("total_count" -> 2)
      repoResponse must */ ("items") /# 1 */("state" -> "open")


    }

    "retrieve an existing issue condition" in {

      val request = serviceRoot / "repos" / "django" / "django" / "issues"
      val myGet = request.GET
      myGet.addParameter("since","2015-02-16T22:03:01Z")
      val response = Http(request OK as.String)
      val repoResponse = response()
      repoResponse must */("url" -> "https://api.github.com/repos/django/django/issues/4157")

      repoResponse must */("number" -> 4157)
      repoResponse must */ ("user") */("login" -> "timgraham")

    }


  }

  "The metrics dashboard service with respect to Stats" should {

    "return success for Issue Stats" in {
      val request = serviceRoot / "repos" / "django" / "django" / "languages"
      println("!!!!!!!!! "+request)
      val myGet = request.GET
      myGet.addParameter("p","counter+language:scala+user:LoyolaChicagoCode+state:open")
      val response = Http(request OK as.String)
      val repoResponse = response()
      repoResponse must / ("Python" -> 10522025)
      repoResponse must / ("CSS" -> 52479)
      repoResponse must / ("JavaScript" -> 106009)
      repoResponse must / ("Makefile" -> 5765)
      repoResponse must / ("Shell" -> 10452)

    }


    "retrieve an existing issue condition" in {

      val request = serviceRoot / "repos" / "username" / "echotest-scala" / "git" / "trees" / "f45e65ff5a1a7b317cf30764fa4190b2e8c2ca0e"
      val myGet = request.GET
      myGet.addParameter("recursive","1")
      val response = Http(request OK as.String)
      val repoResponse = response()
      repoResponse must */("tree") /# 0 /("path" -> ".gitignore")
      repoResponse must */("tree") /# 1 /("path" -> "README.md")
      repoResponse must */("tree") /# 2 /("path" -> "build.sbt")


   }


  }



}