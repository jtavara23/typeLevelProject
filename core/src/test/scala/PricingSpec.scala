package com.pricing.core

import weaver.*
import weaver.scalacheck.Checkers
import org.scalacheck.Gen
import cats.Show
import com.pricing.core.domain.*
import com.pricing.core.pricing.PricingEngine
import java.time.Instant

object PricingSpec extends FunSuite:

  test("computeLineItems calculates correct totals") {
    val items = List(OrderItem(Sku("SKU-001"), 2), OrderItem(Sku("SKU-045"), 1))
    val lineItems = PricingEngine.computeLineItems(items)

    expect(lineItems.length == 2) and
      expect(lineItems(0).unitPrice == BigDecimal("19.99")) and
      expect(lineItems(0).lineTotal == BigDecimal("39.98")) and
      expect(lineItems(1).unitPrice == BigDecimal("49.99")) and
      expect(lineItems(1).lineTotal == BigDecimal("49.99"))
  }

  test("computeSubtotal sums all line totals") {
    val lineItems = List(
      PricedLineItem(Sku("SKU-001"), 2, BigDecimal("19.99"), BigDecimal("39.98")),
      PricedLineItem(Sku("SKU-045"), 1, BigDecimal("49.99"), BigDecimal("49.99"))
    )
    expect(PricingEngine.computeSubtotal(lineItems) == BigDecimal("89.97"))
  }

  test("computeDiscount with no coupon returns zero") {
    expect(PricingEngine.computeDiscount(BigDecimal(100), None) == BigDecimal(0))
  }

  test("computeDiscount with 10% coupon on 89.97") {
    val coupon = Coupon(
      code = CouponCode("TEST10"),
      discountPercent = BigDecimal(10),
      minOrderAmount = BigDecimal(0),
      usageLimit = 100,
      usageCount = 0,
      expiresAt = Instant.MAX,
      stackableWithTier = Set(CustomerTier.GOLD)
    )
    val discount = PricingEngine.computeDiscount(BigDecimal("89.97"), Some(coupon))
    expect(discount == BigDecimal("9.00"))
  }

  test("computeTotal never goes below zero") {
    val result = PricingEngine.computeTotal(BigDecimal(5), BigDecimal(10))
    expect(result == BigDecimal(0))
  }

  test("buildPricedOrder produces correct structure") {
    val order = PricingEngine.buildPricedOrder(
      orderId = OrderId("ord-1"),
      customerId = CustomerId("cust-1"),
      items = List(OrderItem(Sku("SKU-001"), 3)),
      coupon = None,
      now = Instant.parse("2026-07-01T12:00:00Z")
    )
    expect(order.status == OrderStatus.PRICED) and
      expect(order.subtotal == BigDecimal("59.97")) and
      expect(order.discountAmount == BigDecimal(0)) and
      expect(order.total == BigDecimal("59.97")) and
      expect(order.couponApplied.isEmpty)
  }

object PricingPropertySpec extends SimpleIOSuite with Checkers:

  given Show[OrderItem] = Show.fromToString
  given Show[Coupon] = Show.fromToString
  given Show[List[OrderItem]] = Show.fromToString
  given Show[(List[OrderItem], Coupon)] = Show.fromToString

  private val genSku: Gen[Sku] = Gen.oneOf(
    Sku("SKU-001"), Sku("SKU-002"), Sku("SKU-003"),
    Sku("SKU-010"), Sku("SKU-045"), Sku("SKU-100")
  )

  private val genOrderItem: Gen[OrderItem] = for
    sku <- genSku
    qty <- Gen.chooseNum(1, 100)
  yield OrderItem(sku, qty)

  private val genItems: Gen[List[OrderItem]] =
    Gen.listOfN(5, genOrderItem).suchThat(_.nonEmpty)

  private val genCoupon: Gen[Coupon] = for
    pct <- Gen.chooseNum(1, 100)
  yield Coupon(
    code = CouponCode("GEN"),
    discountPercent = BigDecimal(pct),
    minOrderAmount = BigDecimal(0),
    usageLimit = 1000,
    usageCount = 0,
    expiresAt = Instant.MAX,
    stackableWithTier = Set(CustomerTier.GOLD, CustomerTier.SILVER, CustomerTier.BASIC)
  )

  private val genItemsWithCoupon: Gen[(List[OrderItem], Coupon)] = for
    items  <- genItems
    coupon <- genCoupon
  yield (items, coupon)

  test("total is never negative") {
    forall(genItems) { items =>
      val lineItems = PricingEngine.computeLineItems(items)
      val subtotal = PricingEngine.computeSubtotal(lineItems)
      val discount = PricingEngine.computeDiscount(subtotal, None)
      val total = PricingEngine.computeTotal(subtotal, discount)
      expect(total >= BigDecimal(0))
    }
  }

  test("total never exceeds subtotal") {
    forall(genItems) { items =>
      val lineItems = PricingEngine.computeLineItems(items)
      val subtotal = PricingEngine.computeSubtotal(lineItems)
      val total = PricingEngine.computeTotal(subtotal, BigDecimal(0))
      expect(total <= subtotal)
    }
  }

  test("discount never exceeds subtotal") {
    forall(genItemsWithCoupon) { case (items, coupon) =>
      val lineItems = PricingEngine.computeLineItems(items)
      val subtotal = PricingEngine.computeSubtotal(lineItems)
      val discount = PricingEngine.computeDiscount(subtotal, Some(coupon))
      expect(discount <= subtotal)
    }
  }

  test("total with coupon is never negative") {
    forall(genItemsWithCoupon) { case (items, coupon) =>
      val lineItems = PricingEngine.computeLineItems(items)
      val subtotal = PricingEngine.computeSubtotal(lineItems)
      val discount = PricingEngine.computeDiscount(subtotal, Some(coupon))
      val total = PricingEngine.computeTotal(subtotal, discount)
      expect(total >= BigDecimal(0))
    }
  }

  test("total equals subtotal minus discount") {
    forall(genItemsWithCoupon) { case (items, coupon) =>
      val lineItems = PricingEngine.computeLineItems(items)
      val subtotal = PricingEngine.computeSubtotal(lineItems)
      val discount = PricingEngine.computeDiscount(subtotal, Some(coupon))
      val total = PricingEngine.computeTotal(subtotal, discount)
      expect(total == (subtotal - discount).max(BigDecimal(0)))
    }
  }
