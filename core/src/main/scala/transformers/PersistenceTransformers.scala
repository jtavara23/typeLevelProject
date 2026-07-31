package com.pricing.core.transformers

import com.pricing.core.domain.*
import com.pricing.core.persistence.*
import io.scalaland.chimney.dsl.*
import io.scalaland.chimney.Transformer
import java.time.Instant

object PersistenceTransformers:

  given Transformer[CustomerRecord, Customer] =
    Transformer
      .define[CustomerRecord, Customer]
      .withFieldComputed(_.id, src => CustomerId(src.customerId))
      .withFieldComputed(_.tier, src => CustomerTier.valueOf(src.tier))
      .withFieldComputed(_.createdAt, src => Instant.parse(src.createdAt))
      .buildTransformer

  given Transformer[CouponRecord, Coupon] =
    Transformer
      .define[CouponRecord, Coupon]
      .withFieldComputed(_.code, src => CouponCode(src.couponCode))
      .withFieldComputed(_.expiresAt, src => Instant.parse(src.expiresAt))
      .withFieldComputed(_.stackableWithTier, src => src.stackableWithTier.map(CustomerTier.valueOf))
      .buildTransformer

  given Transformer[PricedOrder, OrderRecord] =
    Transformer
      .define[PricedOrder, OrderRecord]
      .withFieldComputed(_.orderId, src => src.orderId.value)
      .withFieldComputed(_.customerId, src => src.customerId.value)
      .withFieldComputed(_.status, src => src.status.toString)
      .withFieldComputed(_.items, src => src.items.map(toPersistenceItem))
      .withFieldComputed(_.couponCode, src => src.couponApplied.map(_.value))
      .withFieldComputed(_.createdAt, src => src.createdAt.toString)
      .withFieldComputed(_.updatedAt, src => src.createdAt.toString)
      .buildTransformer

  private def toPersistenceItem(item: PricedLineItem): OrderItemRecord =
    OrderItemRecord(
      sku = item.sku.value,
      quantity = item.quantity,
      unitPrice = item.unitPrice
    )
