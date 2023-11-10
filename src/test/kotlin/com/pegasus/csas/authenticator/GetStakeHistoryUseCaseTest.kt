package com.pegasus.csas.authenticator

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GetStakeHistoryUseCaseTest {

    private val useCase = GetStakeHistoryUseCase()

    @Test
    fun getHistory() {
        runBlocking {
            val history = useCase.getHistory("addr1qyhrwn3retgf4n6e8cm9exw3pjmpghk4h4pwflfjzs8v3qwz6dlxpqhca3freqtejk23yvmn4xcmayjvhd6h2lq388sq2250rn")

            assertNotNull(history)
            assertTrue((history as GetStakeHistoryUseCase.GetStakeHistoryUseCaseResponse.Result).history.isNotEmpty())
        }
    }

}