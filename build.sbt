javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val root = (project in file(".")).
  settings(
    name := "spark-dynamodb",
    version := "1.0",
    scalaVersion := "2.11.6",
    retrieveManaged := true,
    libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.9.23",
    libraryDependencies += "com.github.seratch" %% "awscala" % "0.5.1",
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.4.1",
    libraryDependencies += "com.google.guava" % "guava" % "14.0.1"
  )