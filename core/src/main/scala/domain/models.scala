package com.pricing.core.domain

import java.time.Instant

final case class Customer(
    id: CustomerId,
    tier: CustomerTier,
    name: Option[String],
    createdAt: Instant
)

final case class Coupon(
    code: CouponCode,
    discountPercent: BigDecimal,
    minOrderAmount: BigDecimal,
    usageLimit: Int,
    usageCount: Int,
    expiresAt: Instant,
    stackableWithTier: Set[CustomerTier]
)

final case class OrderItem(
    sku: Sku,
    quantity: Int
)

final case class PriceRequest(
    customerId: CustomerId,
    items: List[OrderItem],
    couponCode: Option[CouponCode]
)

final case class PricedLineItem(
    sku: Sku,
    quantity: Int,
    unitPrice: BigDecimal,
    lineTotal: BigDecimal
)

final case class PricedOrder(
    orderId: OrderId,
    customerId: CustomerId,
    status: OrderStatus,
    items: List[PricedLineItem],
    subtotal: BigDecimal,
    discountAmount: BigDecimal,
    total: BigDecimal,
    couponApplied: Option[CouponCode],
    createdAt: Instant
)
