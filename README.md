# Spark DynamoDB Library 

## Introduction 
DynamoDB data source for use with Spark SQL.
This [paper](http://web.eecs.umich.edu/~prabal/teaching/resources/eecs582/armbrust15sparksql.pdf) provides good introduction to Spark SQL.
As mentioned in section 4.4 of Spark SQL: Relational Data Processing in Spark paper, the `DefaultSource` class implements `createRelation` function. 
The `DynamoDBRelation` class extends `BaseRelation` with `TableScan` which returns RDD of Rows. 

## Requirements 
- This library requires: 
- Spark 1.4.1
- Scala - 2.10.4
- AWS-Java-SDK - 1.10.11


## Features 
This library reading Amazon DynamoDB tables as Spark DataFrames. API accepts several options: 
- tableName : name of the DynamoDB table
- region: region of the DynamoDB table e.g "eu-west-1"
- scanEntireTable : defaults to true, if set false only first page of items is read. 

If schema is not provided, the API will infer key schema of DynamoDB table which will only include hash and range key attributes. 
Depending on the attributes specified in schema, the API will only read these columns in DynamoDB table. 

## Scala API 
```scala
import org.apache.spark.sql.SQLContext

val schema = StructType(Seq
                    (StructField("name", StringType),
                     StructField("age", LongType)))
                     

val options = mutable.Map[String,String]()
options.put("tableName", "users")
options.put("region", "eu-west-1")
options.put("scanEntireTable", "false")

val df = sqlContext.read.format("com.onzo.spark.dynamodb").schema(schema).options(options).load()

```
## Building from source
This library is built using sbt. To build a uber jar file, simply run sbt assembly from the project root. 