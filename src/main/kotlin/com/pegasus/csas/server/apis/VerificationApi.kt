package com.pegasus.csas.server.apis

import com.google.gson.Gson
import com.pegasus.csas.authenticator.*
import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.cardanoaddress.TransactionTracker
import com.pegasus.csas.server.Paths
import com.pegasus.csas.server.firebaseDb
import com.pegasus.csas.server.model.VerificationStatusPost
import com.pegasus.csas.server.verificationStatusDao
import getServiceProvider
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*

@KtorExperimentalLocationsAPI
fun Route.VerificationApi() {

    val cardanoAddress = CardanoAddress()
    val cardanoGraphql = TransactionTracker()

    val getVerificationStatusUseCase =
        GetVerificationStatusUseCase(verificationStatusDao, firebaseDb, cardanoAddress, cardanoGraphql)

    val createVerificationStatusUseCase =
        CreateVerificationStatusUseCase(verificationStatusDao, firebaseDb, cardanoAddress, cardanoGraphql)

    get { it: Paths.verificationAddressGet ->
        getServiceProvider()?.let { serviceProvider ->
            val userId = call.request.queryParameters["userId"] ?: ""
            val verificationStatusResponse =
                getVerificationStatusUseCase.getVerificationStatus(it.address, userId, serviceProvider)
            handleUseCaseResponse(verificationStatusResponse)
        }
    }

    post { it: Paths.verificationAddressPost ->
        val requestBody = call.receive(VerificationStatusPost::class)
        val userId = requestBody.userId
        val password = requestBody.password
        getServiceProvider()?.let { serviceProvider ->
            val verificationStatusResponse =
                createVerificationStatusUseCase.getVerificationStatus(it.address, userId, serviceProvider, password)
            handleUseCaseResponse(verificationStatusResponse)
        }
    }

}

suspend fun PipelineContext<Unit, ApplicationCall>.handleUseCaseResponse(
    verificationStatusResponse: VerificationStatusResponse
) {
    when (verificationStatusResponse) {
        StatusNotFound -> call.respond(HttpStatusCode.NotFound)
        is InvalidRequest -> call.respond(HttpStatusCode.BadRequest, Gson().toJson(verificationStatusResponse))
        is Status -> call.respond(verificationStatusResponse.status.copy(
            passwordHash = null
        ))
        is AlreadyRegisteredAddress -> call.respond(HttpStatusCode.UnprocessableEntity, Gson().toJson(verificationStatusResponse))
    }
}

