package com.onzo.spark.util

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import org.apache.spark.sql.types.{DataType, DoubleType, StringType}


object DynamoAttributeValue {
  def convert(attributeValue: AttributeValue, dataType:DataType): Option[Any] = {
    if(attributeValue.getS != null) {
      if (dataType.isInstanceOf[DoubleType]) {
        Option(attributeValue.getS.toDouble)
      } else {
        Option(attributeValue.getS)
      }
    }

    else if(attributeValue.getB != null) {
      Option(attributeValue.getB)
    }

    else if(attributeValue.getN != null) {
      Option(attributeValue.getN.toLong)
    }

    else if(attributeValue.getBS != null) {
      Option(attributeValue.getBS)
    }

    else if(attributeValue.getBOOL != null) {
      Option(attributeValue.getBOOL)
    }
    else None
  }
}
