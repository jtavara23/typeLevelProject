package com.pricing.core.transformers

import com.pricing.core.domain.*
import com.pricing.api.{
  PriceOrderInput,
  PriceOrderOutput,
  OrderItemInput,
  PricedItem
}
import io.scalaland.chimney.dsl.*
import io.scalaland.chimney.Transformer

object ApiTransformers:

  given Transformer[OrderItemInput, OrderItem] =
    Transformer
      .define[OrderItemInput, OrderItem]
      .withFieldComputed(_.sku, src => Sku(src.sku))
      .buildTransformer

  def toPriceRequest(input: PriceOrderInput): PriceRequest =
    PriceRequest(
      customerId = CustomerId(input.customerId),
      items = input.items.map(_.transformInto[OrderItem]),
      couponCode = input.couponCode.map(CouponCode(_))
    )

  given Transformer[PricedLineItem, PricedItem] =
    Transformer
      .define[PricedLineItem, PricedItem]
      .withFieldComputed(_.sku, src => src.sku.value)
      .withFieldComputed(_.unitPrice, src => src.unitPrice.toDouble)
      .withFieldComputed(_.lineTotal, src => src.lineTotal.toDouble)
      .buildTransformer

  def toOutput(order: PricedOrder): PriceOrderOutput =
    PriceOrderOutput(
      orderId = order.orderId.value,
      customerId = order.customerId.value,
      status = order.status.toString,
      items = order.items.map(_.transformInto[PricedItem]),
      subtotal = order.subtotal.toDouble,
      discountAmount = order.discountAmount.toDouble,
      total = order.total.toDouble,
      couponApplied = order.couponApplied.map(_.value),
      createdAt = order.createdAt.toString
    )
