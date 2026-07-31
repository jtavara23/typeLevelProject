package com.pricing.service

import cats.effect.{IOApp, IO, ExitCode}

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    IO.println("Pricing Service starting...").as(ExitCode.Success)
