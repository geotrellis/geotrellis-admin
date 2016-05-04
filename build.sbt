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
    "-feature"
    ),
  libraryDependencies ++= Seq(),
  shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
)

lazy val serverSettings = Seq(
  name := "geotrellis-admin-server",
  libraryDependencies ++= Seq(
    "com.azavea"            %% "geotrellis-ingest-test"   % "0.1.0",
    "com.azavea.geotrellis" %% "geotrellis-spark-etl"     % "0.10.0",
    "com.azavea.geotrellis" %% "geotrellis-s3"            % "0.10.0",
    "com.azavea.geotrellis" %% "geotrellis-raster"        % "0.10.0",
    "org.apache.spark"      %% "spark-core"               % "1.5.2" % "provided",
    "io.spray"              %% "spray-routing" % "1.3.3",
    "io.spray"              %% "spray-can"                % "1.3.3",
    "org.scalatest"         %%  "scalatest"               % "2.2.0" % "test"
  ),

  /*dependencyOverrides ++= Set(
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4"
  )*/

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
  name := "geotrellis-admin-client",
  mainClass in (Compile, run) := Some("geotrellis.admin.client.Main"),
  refreshBrowsers <<= refreshBrowsers.triggeredBy(fastOptJS in Compile),
  bootSnippet := "geotrellis.admin.client.Main().main();",
  testFrameworks += new TestFramework("utest.runner.Framework"),
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "utest" % "0.4.3" % "test",
    "com.github.japgolly.scalajs-react" %%% "core" % "0.11.1",
    "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.1",
    "io.circe" %% "circe-core" % "0.4.1",
    "io.circe" %% "circe-generic" % "0.4.1",
    "io.circe" %% "circe-parser" % "0.4.1",
    "io.circe" %% "circe-scalajs_sjs0.6" % "0.4.1",
    "me.chrons" %%% "diode" % "0.5.1",
    "me.chrons" %%% "diode-devtools" % "0.5.1",
    "me.chrons" %%% "diode-react" % "0.5.1",
    "org.scala-js" %%% "scalajs-dom" % "0.9.0",
    "com.github.japgolly.scalacss" %% "core" % "0.4.1",
    "com.github.japgolly.scalacss" %%% "ext-react" % "0.4.1",
    "com.github.chandu0101.scalajs-react-components" %%% "core" % "0.4.1"
  ),
  jsDependencies ++= Seq(
    "org.webjars" % "jquery" % "2.2.1"
    / "jquery.js"
    minified "jquery.min.js"
    commonJSName "jQuery",

    "org.webjars" % "bootstrap" % "3.3.2"
    / "bootstrap.js"
    minified "bootstrap.min.js"
    dependsOn "jquery.js",

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

    "org.webjars.npm" % "react-select" % "1.0.0-beta12"
    /            "react-select.js"
    minified     "react-select.min.js",

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

