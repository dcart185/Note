name := """NotePad"""
organization := "com.dcart185"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += evolutions
libraryDependencies += guice
libraryDependencies += javaJdbc
libraryDependencies += jdbc
libraryDependencies += jdbc % Test
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.8-dmr"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"
libraryDependencies += "io.jsonwebtoken" % "jjwt" % "0.9.0"
//libraryDependencies += "org.bitbucket.b_c" % "jose4j" % "0.6.2"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.dcart185.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.dcart185.binders._"
