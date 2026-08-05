package com.pricing.core.pricing

import com.pricing.core.domain.*
import java.time.Instant

object PricingEngine:

  private val priceTable: Map[Sku, BigDecimal] = Map(
    Sku("SKU-001") -> BigDecimal("19.99"),
    Sku("SKU-002") -> BigDecimal("29.99"),
    Sku("SKU-003") -> BigDecimal("9.99"),
    Sku("SKU-010") -> BigDecimal("14.99"),
    Sku("SKU-045") -> BigDecimal("49.99"),
    Sku("SKU-100") -> BigDecimal("99.99")
  )

  def lookupPrice(sku: Sku): BigDecimal =
    priceTable.getOrElse(sku, BigDecimal(0))

  def computeLineItems(items: List[OrderItem]): List[PricedLineItem] =
    items.map { item =>
      val unitPrice = lookupPrice(item.sku)
      PricedLineItem(
        sku = item.sku,
        quantity = item.quantity,
        unitPrice = unitPrice,
        lineTotal = unitPrice * item.quantity
      )
    }

  def computeSubtotal(lineItems: List[PricedLineItem]): BigDecimal =
    lineItems.map(_.lineTotal).sum

  def computeDiscount(subtotal: BigDecimal, coupon: Option[Coupon]): BigDecimal =
    coupon match
      case Some(c) =>
        val raw = (subtotal * c.discountPercent / 100).setScale(2, BigDecimal.RoundingMode.HALF_UP)
        raw.min(subtotal)
      case None => BigDecimal(0)

  def computeTotal(subtotal: BigDecimal, discount: BigDecimal): BigDecimal =
    (subtotal - discount).max(BigDecimal(0))

  def buildPricedOrder(
      orderId: OrderId,
      customerId: CustomerId,
      items: List[OrderItem],
      coupon: Option[Coupon],
      now: Instant
  ): PricedOrder =
    val lineItems = computeLineItems(items)
    val subtotal = computeSubtotal(lineItems)
    val discount = computeDiscount(subtotal, coupon)
    val total = computeTotal(subtotal, discount)
    PricedOrder(
      orderId = orderId,
      customerId = customerId,
      status = OrderStatus.PRICED,
      items = lineItems,
      subtotal = subtotal,
      discountAmount = discount,
      total = total,
      couponApplied = coupon.map(_.code),
      createdAt = now
    )
