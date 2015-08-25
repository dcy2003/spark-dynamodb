package com.onzo.spark

import org.apache.spark.sql.{ SQLContext }
import org.apache.spark.sql.types.StructType

package object dynamodb {
  implicit class DynamoDBContext(sqlContext: SQLContext) {

    def dynamoDB(tableName: String, region: String, schema: StructType, scanEntireTable: Boolean) = {
      val relation = DynamoDBRelation(tableName, region, Option(schema), scanEntireTable)(sqlContext)
      sqlContext.baseRelationToDataFrame(relation)
    }
    def dynamoDB(tableName: String, region: String) = {
      val relation = DynamoDBRelation(tableName, region)(sqlContext)
      sqlContext.baseRelationToDataFrame(relation)
    }
  }
}
