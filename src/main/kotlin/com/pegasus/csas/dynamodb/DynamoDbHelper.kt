package com.pegasus.csas.dynamodb

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item


class DynamoDbHelper {

    private val client = AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(EndpointConfiguration("https://dynamodb.us-west-2.amazonaws.com", "us-west-2"))
        .build()
    private val dynamoDB = DynamoDB(client)
    private val apiPoolTable = dynamoDB.getTable("api_pool_table")
    private val apiUserTable = dynamoDB.getTable("api_user_table")

    fun insertToApiPoolTable(poolId: String, userId: String) {
        val item = Item()
            .withPrimaryKey("pool_id", poolId)
            .withString("user_id", userId)

        apiPoolTable.putItem(item)
    }

    fun insertToApiUserTable(apiKey: String, userId: String) {
        val item = Item()
            .withPrimaryKey("api_key", apiKey)
            .withString("user_id", userId)

        apiPoolTable.putItem(item)
    }

    fun getUserIdForApiKey(apiKey: String) = apiUserTable.getItem("api_key", apiKey)?.getString("user_id")

}