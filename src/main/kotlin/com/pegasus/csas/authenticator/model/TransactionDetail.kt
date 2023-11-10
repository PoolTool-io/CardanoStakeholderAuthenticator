package com.pegasus.csas.authenticator.model

import java.util.*

data class TransactionDetail(
    val fromStakeKeyHashArray: String,
    val toAddress: String,
    val value: Long,
    val date: Date
)
