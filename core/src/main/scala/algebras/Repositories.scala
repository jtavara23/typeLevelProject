package com.pricing.core.algebras

import com.pricing.core.domain.*

trait CustomerRepository[F[_]]:
  def findById(id: CustomerId): F[Option[Customer]]

trait CouponRepository[F[_]]:
  def findByCode(code: CouponCode): F[Option[Coupon]]

trait OrderRepository[F[_]]:
  def save(order: PricedOrder): F[Unit]
