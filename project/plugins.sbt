addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
addSbtPlugin("com.lihaoyi" % "workbench" % "0.2.3")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.8")

resolvers ++= Seq(
  "twttr" at "https://maven.twttr.com/",
  "spray repo" at "http://repo.spray.io",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.bintrayRepo("azavea", "maven"),
  Classpaths.sbtPluginReleases,
  Opts.resolver.sonatypeReleases
)


