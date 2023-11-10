package com.pegasus.csas.cardanoaddress

import com.pegasus.csas.server.db
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class PoolRewardAddressInspector {

    //Return the poolId this stake address is a reward address for a pool
    suspend fun isRewardAddress(stakeKeyHash: String): String? {
        var poolId: String? = null
        newSuspendedTransaction(db = db) {

            transaction {
                TransactionManager.current().exec(
                    "select pool_id as poolid from pools where reward_account='$stakeKeyHash'"
                ) { resultSet ->
                    while (resultSet.next()) {
                        poolId = resultSet.getString(1)
                    }
                }
            }

            delay(200)
        }
        return poolId
    }

}