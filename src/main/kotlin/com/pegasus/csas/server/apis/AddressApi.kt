package com.pegasus.csas.server.apis

import com.pegasus.csas.cardanoaddress.InspectAddressUseCase
import com.pegasus.csas.cardanoaddress.InspectAddressUseCaseResult
import com.pegasus.csas.server.Paths
import getServiceProvider
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI
fun Route.AddressApi() {

    val inspectAddressUseCase = InspectAddressUseCase()

    get { it: Paths.inspectAddressGet ->
        getServiceProvider()?.let { serviceProvider ->
            when (val inspectionResult = inspectAddressUseCase.inspect(it.address)) {
                InspectAddressUseCaseResult.InvalidAddress -> call.respond(HttpStatusCode.BadRequest)
                is InspectAddressUseCaseResult.Result -> call.respond(inspectionResult.result)
            }
        }
    }
}