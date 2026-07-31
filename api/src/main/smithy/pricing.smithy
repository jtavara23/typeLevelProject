$version: "2"

namespace com.pricing.api

use alloy#simpleRestJson

// =============================================================================
// Service & Operation
// =============================================================================

@simpleRestJson
service PricingService {
    version: "1.0.0"
    operations: [PriceOrder]
}

@http(method: "POST", uri: "/orders/price", code: 200)
operation PriceOrder {
    input: PriceOrderInput
    output: PriceOrderOutput
    errors: [ValidationError]
}

// =============================================================================
// Input
// =============================================================================

structure PriceOrderInput {
    @required
    customerId: String

    @required
    items: OrderItemInputList

    couponCode: String
}

list OrderItemInputList {
    member: OrderItemInput
}

structure OrderItemInput {
    @required
    sku: String

    @required
    quantity: Integer
}

// =============================================================================
// Output
// =============================================================================

structure PriceOrderOutput {
    @required
    orderId: String

    @required
    customerId: String

    @required
    status: String

    @required
    items: PricedItemList

    @required
    subtotal: Double

    @required
    discountAmount: Double

    @required
    total: Double

    couponApplied: String

    @required
    createdAt: String
}

list PricedItemList {
    member: PricedItem
}

structure PricedItem {
    @required
    sku: String

    @required
    quantity: Integer

    @required
    unitPrice: Double

    @required
    lineTotal: Double
}

// =============================================================================
// Errors
// =============================================================================

@error("client")
@httpError(422)
structure ValidationError {
    @required
    errors: ValidationErrorList
}

list ValidationErrorList {
    member: ValidationErrorDetail
}

structure ValidationErrorDetail {
    @required
    code: String

    @required
    field: String

    @required
    message: String
}
