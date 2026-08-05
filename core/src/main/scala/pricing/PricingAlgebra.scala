package com.pricing.core.pricing

import cats.Monad
import cats.data.{EitherT, NonEmptyChain}
import cats.syntax.all.*
import com.pricing.core.domain.*
import com.pricing.core.validation.Validator
import java.time.Instant

trait PricingAlgebra[F[_]]:
  def priceOrder(request: PriceRequest): EitherT[F, NonEmptyChain[AppError], PricedOrder]

object PricingAlgebra:

  def make[F[_]: Monad](
      findCustomer: CustomerId => F[Option[Customer]],
      findCoupon: CouponCode => F[Option[Coupon]],
      generateId: F[OrderId],
      currentTime: F[Instant]
  ): PricingAlgebra[F] = new PricingAlgebra[F]:

    def priceOrder(request: PriceRequest): EitherT[F, NonEmptyChain[AppError], PricedOrder] =
      for
        customer <- lookupCustomer(request.customerId)
        coupon   <- lookupCoupon(request.couponCode)
        now      <- EitherT.liftF(currentTime)
        _        <- validateRequest(request, coupon, customer.tier, now)
        orderId  <- EitherT.liftF(generateId)
        order     = PricingEngine.buildPricedOrder(orderId, request.customerId, request.items, coupon, now)
      yield order

    private def lookupCustomer(id: CustomerId): EitherT[F, NonEmptyChain[AppError], Customer] =
      EitherT(
        findCustomer(id).map(_.toRight(NonEmptyChain.one(AppError.CustomerNotFound(id))))
      )

    private def lookupCoupon(code: Option[CouponCode]): EitherT[F, NonEmptyChain[AppError], Option[Coupon]] =
      code match
        case None => EitherT.rightT(None)
        case Some(c) =>
          EitherT(
            findCoupon(c).map(_.map(Some(_)).toRight(NonEmptyChain.one(AppError.CouponNotFound(c))))
          )

    private def validateRequest(
        request: PriceRequest,
        coupon: Option[Coupon],
        tier: CustomerTier,
        now: Instant
    ): EitherT[F, NonEmptyChain[AppError], Unit] =
      val itemsValidation = Validator.validateItems(request.items)
      val couponValidation = coupon match
        case None    => cats.data.Validated.validNec(())
        case Some(c) =>
          val lineItems = PricingEngine.computeLineItems(request.items)
          val subtotal = PricingEngine.computeSubtotal(lineItems)
          Validator.validateCoupon(c, tier, now, subtotal).void

      val combined = (itemsValidation, couponValidation).mapN((_, _) => ())
      EitherT.fromEither(combined.toEither)
