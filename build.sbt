val scala3Version = "3.6.4"

val catsEffectVersion = "3.5.7"
val http4sVersion = "0.23.30"
val smithy4sVersion = "0.18.25"
val fs2Version = "3.11.0"
val chimneyVersion = "1.6.0"
val cirisVersion = "3.7.0"
val natchezVersion = "0.3.7"
val weaverVersion = "0.8.4"
val scalacheckVersion = "1.18.1"
val logbackVersion = "1.5.15"
val awsSdkVersion = "2.31.1"

ThisBuild / scalaVersion := scala3Version
ThisBuild / organization := "com.pricing"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-Wunused:all",
    "-Wvalue-discard",
    "-feature",
    "-deprecation"
  )
)

lazy val root = (project in file("."))
  .aggregate(api, core, service, lambda, it)
  .settings(
    name := "pricing-service"
  )

lazy val api = (project in file("api"))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(commonSettings)
  .settings(
    name := "pricing-api",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion,
      "com.disneystreaming.smithy4s" %% "smithy4s-json" % smithy4sVersion
    )
  )

lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(
    name := "pricing-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.12.0",
      "io.scalaland" %% "chimney" % chimneyVersion,
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion,
      // Test
      "com.disneystreaming" %% "weaver-cats" % weaverVersion % Test,
      "com.disneystreaming" %% "weaver-scalacheck" % weaverVersion % Test,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion % Test
    ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )
  .dependsOn(api)

lazy val service = (project in file("service"))
  .settings(commonSettings)
  .settings(
    name := "pricing-service-app",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "is.cir" %% "ciris" % cirisVersion,
      "org.tpolecat" %% "natchez-core" % natchezVersion,
      "org.tpolecat" %% "natchez-log" % natchezVersion,
      "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
      "software.amazon.awssdk" % "dynamodb" % awsSdkVersion,
      "software.amazon.awssdk" % "kinesis" % awsSdkVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime
    )
  )
  .dependsOn(core, api)

lazy val lambda = (project in file("lambda"))
  .settings(commonSettings)
  .settings(
    name := "pricing-lambda",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "io.scalaland" %% "chimney" % chimneyVersion,
      "software.amazon.awssdk" % "kinesis" % awsSdkVersion,
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.3",
      "com.amazonaws" % "aws-lambda-java-events" % "3.14.0"
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    },
    assembly / assemblyJarName := "stream-processor.jar"
  )
  .dependsOn(core)

lazy val it = (project in file("it"))
  .settings(commonSettings)
  .settings(
    name := "pricing-integration-tests",
    libraryDependencies ++= Seq(
      "com.disneystreaming" %% "weaver-cats" % weaverVersion % Test,
      "org.http4s" %% "http4s-ember-client" % http4sVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-localstack-v2" % "0.41.8" % Test,
      "software.amazon.awssdk" % "dynamodb" % awsSdkVersion % Test,
      "software.amazon.awssdk" % "kinesis" % awsSdkVersion % Test
    ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )
  .dependsOn(service)
