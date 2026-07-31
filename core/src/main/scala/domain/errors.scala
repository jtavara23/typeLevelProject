package com.pricing.core.domain

enum AppError:
  case UnknownSku(sku: Sku)
  case InvalidQuantity(sku: Sku, quantity: Int)
  case CouponExpired(code: CouponCode, expiredAt: String)
  case CouponOverused(code: CouponCode, usageLimit: Int)
  case StackingNotAllowed(code: CouponCode, tier: CustomerTier)
  case CustomerNotFound(id: CustomerId)
  case CouponNotFound(code: CouponCode)
  case MinOrderAmountNotMet(code: CouponCode, required: BigDecimal, actual: BigDecimal)
