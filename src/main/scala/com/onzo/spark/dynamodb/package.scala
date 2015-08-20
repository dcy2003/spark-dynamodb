package com.onzo.spark

import org.apache.spark.sql.{SQLContext}
import org.apache.spark.sql.types.StructType


package object dynamodb {

  implicit class DynamoDBContext(sqlContext: SQLContext) {

    def dynamoDB(tableName: String, region: String, schema: StructType, rateLimit: Double, permissionToConsume: Int, scanEntireTable: Boolean) = {
      val relation = DynamoDbRelation(tableName, region, Option(schema), rateLimit, permissionToConsume,scanEntireTable)(sqlContext)
      sqlContext.baseRelationToDataFrame(relation)
    }

    def dynamoDB(tableName:String,region:String): Unit ={
      val relation = DynamoDbRelation(tableName, region)(sqlContext)
      sqlContext.baseRelationToDataFrame(relation)
    }
  }

}
