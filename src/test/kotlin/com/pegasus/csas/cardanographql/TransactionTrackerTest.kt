package com.pegasus.csas.cardanographql

import com.pegasus.csas.cardanoaddress.TransactionTracker
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class TransactionTrackerTest {

    val cardanoGraphql = TransactionTracker()

    @Test
    fun testAddressTxs() {
        runBlocking {
            val address =
                "addr1qykeqj78jj98ulnkmjt0dxh5ya4pcw27qy7l4z5dkzyr5grszxlk5cj4w7utxxngjzjc2rc0ht9hmxr0zh493gyyks0qh0cexs"

            val txs = cardanoGraphql.getTransactionsForAddress(address)

            assert(txs.isNotEmpty())
            assert(txs[0].value == 1090000L)
            assert(txs[0].toAddress == address)
            assertEquals(Date(1658250842), txs[0].date)
            assert(txs[0].fromStakeKeyHashArray == "{955831e7ba81c0e31f871272cef103b9ac309dbd80bea3421b94f6c7}")
        }
    }

    @Test
    fun testAddressMultipleTxs() {
        runBlocking {
            val address =
                "addr1qxyw3frndjjjwpad4shxs8ltdgqln5qnjtvazqjjznj4qxwz6dlxpqhca3freqtejk23yvmn4xcmayjvhd6h2lq388sqgr0ng0"

            val txs = cardanoGraphql.getTransactionsForAddress(address)

            assertEquals(9, txs.size)
        }
    }
}