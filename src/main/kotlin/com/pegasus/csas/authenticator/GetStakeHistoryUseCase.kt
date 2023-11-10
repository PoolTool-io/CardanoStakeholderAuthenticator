package com.pegasus.csas.authenticator

import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.firebase.getValue
import com.pegasus.csas.server.model.StakeHistory

class GetStakeHistoryUseCase() {

    private val firebaseDb = FirebaseDb()
    private val cardanoAddress = CardanoAddress()

    suspend fun getHistory(address: String): GetStakeHistoryUseCaseResponse {
        return try {
            val stakeKeyHash = cardanoAddress.inspect(address)?.stakeKeyHash
                ?: return GetStakeHistoryUseCaseResponse.InvalidAddress
            val firebaseStakeHistory =
                firebaseDb.stakeHistory.child(stakeKeyHash).getValue().value as (Map<String, Map<String, Any>>)
            GetStakeHistoryUseCaseResponse.Result(firebaseStakeHistory.map {
                StakeHistory(
                    epoch = it.key.toInt(),
                    delegatedTo = it.value["delegatedTo"].toString(),
                    amount = it.value["amount"].toString().toLong()
                )
            }.sortedBy { it.epoch })
        } catch (e: Exception) {
            println("GetStakeHistoryUseCase failed to get history!")
            e.printStackTrace()
            GetStakeHistoryUseCaseResponse.AddressNotFound
        }
    }

    sealed class GetStakeHistoryUseCaseResponse {
        object InvalidAddress : GetStakeHistoryUseCaseResponse()
        object AddressNotFound : GetStakeHistoryUseCaseResponse()
        class Result(val history: List<StakeHistory>) : GetStakeHistoryUseCaseResponse()
    }

}


