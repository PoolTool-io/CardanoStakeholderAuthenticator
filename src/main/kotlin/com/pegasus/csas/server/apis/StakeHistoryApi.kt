package com.pegasus.csas.server.apis

import com.pegasus.csas.authenticator.GetStakeHistoryUseCase
import com.pegasus.csas.server.Paths
import getServiceProvider
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI
fun Route.StakeHistoryApi() {

    val getStakeHistoryUseCase = GetStakeHistoryUseCase()

    get { it: Paths.stakehistoryAddressGet ->
        getServiceProvider()?.let { serviceProvider ->
            when (val stakeHistoryResult = getStakeHistoryUseCase.getHistory(it.address)) {
                GetStakeHistoryUseCase.GetStakeHistoryUseCaseResponse.InvalidAddress -> call.respond(HttpStatusCode.BadRequest)
                GetStakeHistoryUseCase.GetStakeHistoryUseCaseResponse.AddressNotFound -> call.respond(HttpStatusCode.NotFound)
                is GetStakeHistoryUseCase.GetStakeHistoryUseCaseResponse.Result -> call.respond(stakeHistoryResult.history)
            }
        }
    }
}