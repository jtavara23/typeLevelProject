package com.pricing.service.config

import cats.effect.IO
import cats.syntax.all.*
import ciris.*

final case class AppConfig(
    ordersTable: String,
    customersTable: String,
    couponsTable: String,
    kinesisStream: String,
    awsRegion: String,
    localstackEndpoint: Option[String],
    httpPort: Int,
    loyaltyPartnerUrl: String
)

object AppConfig:

  val load: IO[AppConfig] =
    (
      env("ORDERS_TABLE").default("Orders"),
      env("CUSTOMERS_TABLE").default("Customers"),
      env("COUPONS_TABLE").default("Coupons"),
      env("KINESIS_STREAM").default("order-priced-events"),
      env("AWS_REGION").default("us-east-1"),
      env("LOCALSTACK_ENDPOINT").option,
      env("HTTP_PORT").as[Int].default(8080),
      env("LOYALTY_PARTNER_URL").default("http://localhost:9090/loyalty")
    ).parMapN(AppConfig.apply).load[IO]
