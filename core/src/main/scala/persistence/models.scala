package com.pricing.core.persistence

final case class OrderRecord(
    orderId: String,
    customerId: String,
    status: String,
    items: List[OrderItemRecord],
    subtotal: BigDecimal,
    discountAmount: BigDecimal,
    total: BigDecimal,
    couponCode: Option[String],
    createdAt: String,
    updatedAt: String
)

final case class OrderItemRecord(
    sku: String,
    quantity: Int,
    unitPrice: BigDecimal
)

final case class CustomerRecord(
    customerId: String,
    tier: String,
    name: Option[String],
    createdAt: String
)

final case class CouponRecord(
    couponCode: String,
    discountPercent: BigDecimal,
    minOrderAmount: BigDecimal,
    usageLimit: Int,
    usageCount: Int,
    expiresAt: String,
    stackableWithTier: Set[String]
)
