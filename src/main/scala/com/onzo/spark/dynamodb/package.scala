package com.onzo.spark

import org.apache.spark.sql.{ SQLContext }
import org.apache.spark.sql.types.StructType

package object dynamodb {
  implicit class DynamoDBContext(sqlContext: SQLContext) {

    def dynamoDB(tableName: String, region: String, accessKeyId: String, secretAccessKey: String, schema: StructType, scanEntireTable: Boolean) = {
      val relation = DynamoDBRelation(tableName, region, accessKeyId, secretAccessKey, Option(schema), scanEntireTable)(sqlContext)
      sqlContext.baseRelationToDataFrame(relation)
    }
    def dynamoDB(tableName: String, region: String, accessKeyId: String, secretAccessKey: String) = {
      val relation = DynamoDBRelation(tableName, region, accessKeyId, secretAccessKey)(sqlContext)
      sqlContext.baseRelationToDataFrame(relation)
    }
  }
}
