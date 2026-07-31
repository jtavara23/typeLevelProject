package com.pricing.core.domain

opaque type CustomerId = String
object CustomerId:
  def apply(value: String): CustomerId = value
  extension (id: CustomerId) def value: String = id

opaque type OrderId = String
object OrderId:
  def apply(value: String): OrderId = value
  extension (id: OrderId) def value: String = id

opaque type CouponCode = String
object CouponCode:
  def apply(value: String): CouponCode = value
  extension (code: CouponCode) def value: String = code

opaque type Sku = String
object Sku:
  def apply(value: String): Sku = value
  extension (sku: Sku) def value: String = sku
