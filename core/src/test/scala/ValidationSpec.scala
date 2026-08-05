package com.pricing.core

import weaver.*
import cats.data.Validated
import com.pricing.core.domain.*
import com.pricing.core.validation.Validator
import java.time.Instant

object ValidationSpec extends FunSuite:

  val now: Instant = Instant.parse("2026-07-01T12:00:00Z")

  val validCoupon: Coupon = Coupon(
    code = CouponCode("SUMMER10"),
    discountPercent = BigDecimal(10),
    minOrderAmount = BigDecimal(50),
    usageLimit = 100,
    usageCount = 42,
    expiresAt = Instant.parse("2027-01-01T00:00:00Z"),
    stackableWithTier = Set(CustomerTier.GOLD, CustomerTier.SILVER)
  )

  test("valid items pass validation") {
    val items = List(OrderItem(Sku("SKU-001"), 2), OrderItem(Sku("SKU-045"), 1))
    val result = Validator.validateItems(items)
    expect(result.isValid)
  }

  test("unknown SKU is rejected") {
    val items = List(OrderItem(Sku("SKU-999"), 1))
    val result = Validator.validateItems(items)
    result match
      case Validated.Invalid(errors) =>
        expect(errors.exists(_.isInstanceOf[AppError.UnknownSku]))
      case _ => failure("expected invalid")
  }

  test("zero quantity is rejected") {
    val items = List(OrderItem(Sku("SKU-001"), 0))
    val result = Validator.validateItems(items)
    result match
      case Validated.Invalid(errors) =>
        expect(errors.exists(_.isInstanceOf[AppError.InvalidQuantity]))
      case _ => failure("expected invalid")
  }

  test("multiple errors are accumulated") {
    val items = List(OrderItem(Sku("SKU-999"), 0), OrderItem(Sku("BAD"), -1))
    val result = Validator.validateItems(items)
    result match
      case Validated.Invalid(errors) =>
        expect(errors.length >= 3)
      case _ => failure("expected invalid")
  }

  test("valid coupon passes all checks") {
    val result = Validator.validateCoupon(validCoupon, CustomerTier.GOLD, now, BigDecimal(100))
    expect(result.isValid)
  }

  test("expired coupon is rejected") {
    val expired = validCoupon.copy(expiresAt = Instant.parse("2025-01-01T00:00:00Z"))
    val result = Validator.validateCoupon(expired, CustomerTier.GOLD, now, BigDecimal(100))
    result match
      case Validated.Invalid(errors) =>
        expect(errors.exists(_.isInstanceOf[AppError.CouponExpired]))
      case _ => failure("expected invalid")
  }

  test("overused coupon is rejected") {
    val overused = validCoupon.copy(usageCount = 100)
    val result = Validator.validateCoupon(overused, CustomerTier.GOLD, now, BigDecimal(100))
    result match
      case Validated.Invalid(errors) =>
        expect(errors.exists(_.isInstanceOf[AppError.CouponOverused]))
      case _ => failure("expected invalid")
  }

  test("tier stacking not allowed is rejected") {
    val result = Validator.validateCoupon(validCoupon, CustomerTier.BASIC, now, BigDecimal(100))
    result match
      case Validated.Invalid(errors) =>
        expect(errors.exists(_.isInstanceOf[AppError.StackingNotAllowed]))
      case _ => failure("expected invalid")
  }

  test("subtotal below minimum order amount is rejected") {
    val result = Validator.validateCoupon(validCoupon, CustomerTier.GOLD, now, BigDecimal(10))
    result match
      case Validated.Invalid(errors) =>
        expect(errors.exists(_.isInstanceOf[AppError.MinOrderAmountNotMet]))
      case _ => failure("expected invalid")
  }

  test("coupon with multiple failures accumulates all errors") {
    val bad = validCoupon.copy(
      usageCount = 100,
      expiresAt = Instant.parse("2025-01-01T00:00:00Z")
    )
    val result = Validator.validateCoupon(bad, CustomerTier.BASIC, now, BigDecimal(10))
    result match
      case Validated.Invalid(errors) =>
        expect(errors.length == 4)
      case _ => failure("expected invalid")
  }
