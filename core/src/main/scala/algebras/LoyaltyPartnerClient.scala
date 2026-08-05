package com.pricing.core.algebras

import com.pricing.core.domain.*

final case class PartnerPerk(description: String, additionalDiscount: BigDecimal)

trait LoyaltyPartnerClient[F[_]]:
  def checkPerk(customerId: CustomerId): F[Option[PartnerPerk]]
