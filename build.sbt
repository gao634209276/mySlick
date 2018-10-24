name := "mySlick"

version := "0.1"

scalaVersion := sys.env.getOrElse("SCALA_VERSION", "2.11.8")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies += "com.typesafe.slick" %% "slick" % "3.1.1"

libraryDependencies += "com.h2database" % "h2" % "1.3.176"

libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1"

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.10"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.42"

libraryDependencies += "commons-dbcp" % "commons-dbcp" % "1.4"

libraryDependencies += "org.flywaydb" % "flyway-core" % "3.2.1"

libraryDependencies += "io.spray" %% "spray-json" % "1.3.2"