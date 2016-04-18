lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "Azavea",
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  parallelExecution in Test := false,
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-Yinline-warnings",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:existentials",
    "-feature"),
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core" % "0.4.1",
    "io.circe" %% "circe-generic" % "0.4.1",
    "io.circe" %% "circe-parser" % "0.4.1"
  )
)

lazy val serverSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest"         %%  "scalatest"           % "2.2.0"    % "test",
    "com.twitter"           %% "finagle-http"         % "6.34.0",
    "com.twitter"           %% "twitter-server"       % "1.19.0",
    "com.typesafe.akka"     %% "akka-http-core"       % "2.4.4",
    "com.typesafe.akka"     %% "akka-http-experimental" % "2.4.4",
    "de.heikoseeberger"     %% "akka-http-circe"      % "1.6.0",
    "log4j"                 %  "log4j"                % "1.2.14",
    "org.apache.spark"      %% "spark-core"           % "1.5.2",
    "com.azavea.geotrellis" %% "geotrellis-accumulo"  % "0.10.0-RC4",
    "com.azavea.geotrellis" %% "geotrellis-raster"    % "0.10.0-RC4",
    "com.azavea.geotrellis" %% "geotrellis-s3"        % "0.10.0-RC4",
    "com.azavea.geotrellis" %% "geotrellis-spark"     % "0.10.0-RC4",
    "com.azavea.geotrellis" %% "geotrellis-spark-etl" % "0.10.0-RC4"
  ),

  dependencyOverrides ++= Set(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4"
  ),

  shellPrompt := { s => Project.extract(s).currentProject.id + " > " },

  assemblyMergeStrategy in assembly := {
    case "reference.conf" => MergeStrategy.concat
    case "application.conf" => MergeStrategy.concat
    case "META-INF/MANIFEST.MF" => MergeStrategy.discard
    case "META-INF\\MANIFEST.MF" => MergeStrategy.discard
    case "META-INF/ECLIPSEF.RSA" => MergeStrategy.discard
    case "META-INF/ECLIPSEF.SF" => MergeStrategy.discard
    case _ => MergeStrategy.first
  }

)

lazy val clientSettings = Seq(
  mainClass in (Compile, run) := Some("geotrellis.admin.client.Main"),
  refreshBrowsers <<= refreshBrowsers.triggeredBy(fastOptJS in Compile),
  bootSnippet := "geotrellis.admin.client.Main().main();",
  testFrameworks += new TestFramework("utest.runner.Framework"),
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "utest" % "0.4.3" % "test",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.11.0",
    "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.0",
    "io.circe" %% "circe-core" % "0.4.1",
    "io.circe" %% "circe-generic" % "0.4.1",
    "io.circe" %% "circe-parser" % "0.4.1",
    "io.circe" %% "circe-scalajs_sjs0.6" % "0.4.1",
    "com.lihaoyi" %%% "upickle" % "0.3.9",
    "me.chrons" %%% "diode" % "0.5.1",
    "me.chrons" %%% "diode-devtools" % "0.5.1",
    "me.chrons" %%% "diode-react" % "0.5.1",
    "org.scala-js" %%% "scalajs-dom" % "0.9.0",
    "org.querki" %%% "jquery-facade" % "1.0-RC3"
  ),
  jsDependencies ++= Seq(
    "org.webjars" % "jquery" % "2.2.1"
    / "jquery.js" minified "jquery.min.js"
    commonJSName "jQuery",

    "org.webjars.bower" % "react" % "15.0.1"
      /        "react-with-addons.js"
      minified "react-with-addons.min.js"
      commonJSName "React",

    "org.webjars.bower" % "react" % "15.0.1"
      /         "react-dom.js"
      minified  "react-dom.min.js"
      dependsOn "react-with-addons.js"
      commonJSName "ReactDOM",

    "org.webjars.bower" % "react" % "15.0.1"
      /         "react-dom-server.js"
      minified  "react-dom-server.min.js"
      dependsOn "react-dom.js"
      commonJSName "ReactDOMServer",

    "org.webjars.bower" % "leaflet" % "1.0.0-beta.2"
      /         "leaflet.js"
      commonJSName "L")
)

lazy val root = Project("geotrellis-admin", file(".")).
  dependsOn(
    client,
    server
  ).
  settings(commonSettings: _*).
  settings(
    initialCommands in console :=
      """
      """
  )

lazy val server = Project("server", file("server")).
  dependsOn(sharedJVM).
  settings(commonSettings: _*).
  settings(serverSettings: _*)

lazy val client = Project("client", file("client")).
  dependsOn(sharedJS).
  settings(commonSettings: _*).
  settings(clientSettings: _*).
  settings(workbenchSettings: _*).
  enablePlugins(ScalaJSPlugin)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(commonSettings: _*).
  jvmSettings(
    name := "sharedJVM",
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
  ).
  jsSettings(
    name := "sharedJS",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0"
  )

lazy val sharedJVM = shared.jvm

lazy val sharedJS = shared.js

