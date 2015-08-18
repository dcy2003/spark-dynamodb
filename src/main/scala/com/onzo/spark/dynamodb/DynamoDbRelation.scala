package com.onzo.spark.dynamodb


import com.google.common.util.concurrent.RateLimiter
import com.onzo.spark.util.{ReservedWords, DynamoAttributeValue}
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model.{ReturnConsumedCapacity, AttributeDefinition, ScanRequest}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.sources.{TableScan, BaseRelation}
import org.apache.spark.sql.types.BinaryType
import org.apache.spark.sql.types.LongType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType

import scala.collection.JavaConversions._
import scala.collection.mutable



case class DynamoDbRelation(tableName: String, region: String, schemaP: Option[StructType] = None,
                            rateLimitP: Option[Double] = None, permissionToConsumeP: Option[Int] = None)
                            (sqlContextP: SQLContext) extends BaseRelation with TableScan {

  @transient val credentials = new DefaultAWSCredentialsProviderChain().getCredentials
  @transient val dynamoDbClient = new AmazonDynamoDBClient(credentials)
  dynamoDbClient.setRegion(Regions.fromName(region))

  val dynamoDbTable = dynamoDbClient.describeTable(tableName)

  override def sqlContext = sqlContextP

  override val schema: StructType = schemaP match {
    case Some(struct: StructType) => struct
    case _ => StructType(getSchema(dynamoDbTable.getTable.getAttributeDefinitions))
  }

  val rateLimit: Double = rateLimitP match {
    case Some(rateL: Double) => rateL
    case _ => 25.0
  }

  var permissionToConsume: Int = permissionToConsumeP match {
    case Some(consume: Int) => consume
    case _ => 1
  }

  val projectionExpression = {
    val expression = new StringBuilder()
    val expressionNames = mutable.Map[String, String]()
    schema.fieldNames.map { fieldName =>
      if (ReservedWords.reservedWords.contains(fieldName.toUpperCase)) {
        val key = "#".concat(fieldName)
        expression.append(key).append(",")
        expressionNames += (key -> fieldName)
      } else {
        expression.append(fieldName).append(",")
      }
    }
    if (expressionNames.nonEmpty) {
      (expression.toString().dropRight(1), Option(expressionNames))
    } else {
      (expression.toString().dropRight(1), None)
    }
  }

  override def buildScan: RDD[Row] = {
    val rateLimiter = RateLimiter.create(rateLimit)
    rateLimiter.acquire(permissionToConsume)
    val scanRequest =
      new ScanRequest()
        .withTableName(tableName)
        .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
        .withProjectionExpression(projectionExpression._1)
        .withExpressionAttributeNames(projectionExpression._2.get)
    var scanResult = dynamoDbClient.scan(scanRequest)
    val results = scanResult.getItems
    permissionToConsume = (scanResult.getConsumedCapacity.getCapacityUnits - 1.0).toInt
    if (permissionToConsume <= 0) permissionToConsume = 1


    while (scanResult.getLastEvaluatedKey != null) {
      rateLimiter.acquire(permissionToConsume)
      scanRequest.setExclusiveStartKey(scanResult.getLastEvaluatedKey)
      scanResult = dynamoDbClient.scan(scanRequest)

      permissionToConsume = (scanResult.getConsumedCapacity.getCapacityUnits - 1.0).toInt

      if (permissionToConsume <= 0) permissionToConsume = 1
      results.addAll(scanResult.getItems)
    }

    val rowRDD = sqlContext.sparkContext.parallelize(results)
    rowRDD.map { result =>
      val values = schema.fieldNames.map { fieldName =>
        DynamoAttributeValue.convert(result.get(fieldName))
      }
      Row.fromSeq(values)
    }
  }

  private def getSchema(attributeDefinitions: Seq[AttributeDefinition]): Seq[StructField] = {
    attributeDefinitions.map { attributeDefinition =>
      attributeDefinition.getAttributeType match {
        case "S" => StructField(attributeDefinition.getAttributeName, StringType)
        case "N" => StructField(attributeDefinition.getAttributeName, LongType)
        case "B" => StructField(attributeDefinition.getAttributeName, BinaryType)
        case other => sys.error(s"Unsupported $other")
      }
    }
  }
}

