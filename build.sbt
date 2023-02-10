lazy val commonSettings = Seq(
  scalaVersion := "2.13.10",
)

lazy val root = (project in file(".")).aggregate(logProcessor, logConsumer)

lazy val logProcessor = (project in file("log-processor"))
  .settings(commonSettings)
  .settings(
    name := "log-processor",
    libraryDependencies ++= Seq(
        "org.apache.kafka" % "kafka-streams-scala_2.13" % "3.4.0",
        "org.slf4j" % "slf4j-api" % "2.0.6",
        "org.slf4j" % "slf4j-simple" % "2.0.6" 
    )
  )

lazy val logConsumer = (project in file("log-consumer"))
  .settings(commonSettings)
  .settings(
    name := "log-consumer",
    libraryDependencies ++= Seq(
        "org.apache.kafka" % "kafka-streams-scala_2.13" % "3.4.0"
    )
  )

    