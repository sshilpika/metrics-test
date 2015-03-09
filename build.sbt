name := "metrics-test"

version := "0.1"

scalaVersion := "2.11.4"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  )
}

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "2.14",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2" % Test
)

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.10",
  "org.mindrot" % "jbcrypt" % "0.3m"
)

// IntelliJ Scala plugin reports false positive error here
enablePlugins(JavaAppPackaging)
