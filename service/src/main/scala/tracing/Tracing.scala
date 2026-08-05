package com.pricing.service.tracing

import cats.effect.{IO, Resource}
import natchez.EntryPoint
import natchez.log.Log
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Tracing:

  given Logger[IO] = Slf4jLogger.getLoggerFromName[IO]("pricing-service")

  val entryPoint: EntryPoint[IO] =
    Log.entryPoint[IO]("pricing-service")

  val entryPointResource: Resource[IO, EntryPoint[IO]] =
    Resource.pure(entryPoint)
