package com.pricing.core.validation

import cats.data.ValidatedNec
import cats.syntax.all.*
import com.pricing.core.domain.*
import java.time.Instant

object Validator:

  type ValidationResult[A] = ValidatedNec[AppError, A]

  private val knownSkus: Set[Sku] = Set(
    Sku("SKU-001"), Sku("SKU-002"), Sku("SKU-003"),
    Sku("SKU-010"), Sku("SKU-045"), Sku("SKU-100")
  )

  def validateItems(items: List[OrderItem]): ValidationResult[List[OrderItem]] =
    items.traverse(validateItem)

  private def validateItem(item: OrderItem): ValidationResult[OrderItem] =
    (validateSku(item.sku), validateQuantity(item)).mapN((_, _) => item)

  private def validateSku(sku: Sku): ValidationResult[Sku] =
    if knownSkus.contains(sku) then sku.validNec
    else AppError.UnknownSku(sku).invalidNec

  private def validateQuantity(item: OrderItem): ValidationResult[Int] =
    if item.quantity > 0 then item.quantity.validNec
    else AppError.InvalidQuantity(item.sku, item.quantity).invalidNec

  def validateCoupon(
      coupon: Coupon,
      tier: CustomerTier,
      now: Instant,
      subtotal: BigDecimal
  ): ValidationResult[Coupon] =
    (
      validateCouponExpiry(coupon, now),
      validateCouponUsage(coupon),
      validateCouponStacking(coupon, tier),
      validateMinOrderAmount(coupon, subtotal)
    ).mapN((_, _, _, _) => coupon)

  private def validateCouponExpiry(coupon: Coupon, now: Instant): ValidationResult[Unit] =
    if now.isBefore(coupon.expiresAt) then ().validNec
    else AppError.CouponExpired(coupon.code, coupon.expiresAt.toString).invalidNec

  private def validateCouponUsage(coupon: Coupon): ValidationResult[Unit] =
    if coupon.usageCount < coupon.usageLimit then ().validNec
    else AppError.CouponOverused(coupon.code, coupon.usageLimit).invalidNec

  private def validateCouponStacking(coupon: Coupon, tier: CustomerTier): ValidationResult[Unit] =
    if coupon.stackableWithTier.contains(tier) then ().validNec
    else AppError.StackingNotAllowed(coupon.code, tier).invalidNec

  private def validateMinOrderAmount(coupon: Coupon, subtotal: BigDecimal): ValidationResult[Unit] =
    if subtotal >= coupon.minOrderAmount then ().validNec
    else AppError.MinOrderAmountNotMet(coupon.code, coupon.minOrderAmount, subtotal).invalidNec
