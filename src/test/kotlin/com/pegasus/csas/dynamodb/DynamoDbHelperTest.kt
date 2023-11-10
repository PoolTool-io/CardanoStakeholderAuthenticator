package com.pegasus.csas.dynamodb

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DynamoDbHelperTest {

    private val dynamoDbHelper = DynamoDbHelper()

    @Test
    fun testGetUserIdForApiKey() {
        val userId = dynamoDbHelper.getUserIdForApiKey("5f95d5f2-41ab-40b5-8e25-7ceb1735e4d7")
        assertEquals("fe97f866-b7c2-4444-8e86-079556dc2c6e", userId)
    }

    @Test
    fun testGetUserIdForApiKeyUnknown() {
        val userId = dynamoDbHelper.getUserIdForApiKey("non-existent")
        assertEquals(null, userId)
    }

}