package com.pricing.lambda

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent

class StreamProcessorHandler extends RequestHandler[DynamodbEvent, String]:
  override def handleRequest(event: DynamodbEvent, context: Context): String =
    s"Processed ${event.getRecords.size()} records"
