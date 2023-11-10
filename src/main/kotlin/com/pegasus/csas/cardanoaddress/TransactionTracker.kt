package com.pegasus.csas.cardanoaddress

import com.pegasus.csas.authenticator.model.TransactionDetail
import com.pegasus.csas.server.db
import kotlinx.coroutines.delay
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class TransactionTracker {

    suspend fun getTransactionsForAddress(address: String): List<TransactionDetail> {
        var transactionDetails: List<TransactionDetail>? = null
        newSuspendedTransaction(db = db) {

            transaction {

                //id |                                               to_address                                                | amount  | timestamp  |                                            from_address_array                                             |                    from_stakekey_array                     | processed
                //select to_address, amount, timestamp, from_stakekey_array, processed from auth_watch where processed=false AND to_address='addr1qykeqj78jj98ulnkmjt0dxh5ya4pcw27qy7l4z5dkzyr5grszxlk5cj4w7utxxngjzjc2rc0ht9hmxr0zh493gyyks0qh0cexs';
                TransactionManager.current().exec(
                    "select to_address, amount, timestamp, from_stakekey_array, processed " +
                            "from auth_watch where processed=false AND to_address='$address';"
                ) { resultSet ->
                    val mutableTransactionDetails = mutableListOf<TransactionDetail>()
                    while (resultSet.next()) {
                        mutableTransactionDetails.add(
                            TransactionDetail(
                                fromStakeKeyHashArray = resultSet.getString(4),
                                toAddress = resultSet.getString(1),
                                value = resultSet.getLong(2),
                                date = Date(resultSet.getLong(3))
                            )
                        )
                    }
                    transactionDetails = mutableTransactionDetails.toList()
                }
            }



            while (transactionDetails == null) {
                delay(200)
            }
        }
        return transactionDetails ?: emptyList()
    }

}