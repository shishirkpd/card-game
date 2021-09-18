lazy val akkaHttpVersion = "10.2.6"
lazy val akkaVersion    = "2.6.16"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.13.4"
    )),
    name := "card-games",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"         %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka"         %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka"         %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka"         %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"            % "logback-classic"           % "1.2.3",
      "com.softwaremill.macwire"  %% "macros"                   % "2.3.2"         % "provided",

      "com.typesafe.akka"         %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka"         %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"             %% "scalatest"                % "3.1.4"         % Test,
      "org.scalatestplus"         %% "mockito-3-4"              % "3.2.9.0"       % Test
    )
  )