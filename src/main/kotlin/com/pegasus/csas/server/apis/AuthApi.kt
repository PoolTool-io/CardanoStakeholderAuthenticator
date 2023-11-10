package com.pegasus.csas.server.apis

import com.pegasus.csas.authenticator.LoginUserUseCase
import com.pegasus.csas.authenticator.forgotpassword.CreateForgotPasswordStatusUseCase
import com.pegasus.csas.authenticator.forgotpassword.GetForgotPasswordStatusUseCase
import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.cardanoaddress.TransactionTracker
import com.pegasus.csas.server.Paths
import com.pegasus.csas.server.firebaseDb
import com.pegasus.csas.server.forgotPasswordStatusDao
import com.pegasus.csas.server.model.AuthLoginPost
import com.pegasus.csas.server.model.ForgotPasswordStatusPost
import getServiceProvider
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

@KtorExperimentalLocationsAPI
fun Route.AuthApi() {

    val cardanoAddress = CardanoAddress()
    val cardanoGraphql = TransactionTracker()

    val getForgotPasswordStatusUseCase =
        GetForgotPasswordStatusUseCase(forgotPasswordStatusDao, firebaseDb, cardanoAddress, cardanoGraphql)

    val createForgotPasswordStatusUseCase =
        CreateForgotPasswordStatusUseCase(forgotPasswordStatusDao, firebaseDb, cardanoAddress, cardanoGraphql)

    val loginUseCase = LoginUserUseCase(firebaseDb)

    post { it: Paths.authLoginPost ->
        getServiceProvider()?.let { serviceProvider ->
            val requestBody = call.receive(AuthLoginPost::class)
            val loginResponse = loginUseCase.login(requestBody.address, requestBody.password)
            when (loginResponse) {
                LoginUserUseCase.LoginUserUseCaseResponse.InvalidAddress -> call.respond(HttpStatusCode.BadRequest)
                LoginUserUseCase.LoginUserUseCaseResponse.UnknownError -> call.respond(HttpStatusCode.InternalServerError)
                LoginUserUseCase.LoginUserUseCaseResponse.Unauthenticated -> call.respond(HttpStatusCode.Unauthorized)
                is LoginUserUseCase.LoginUserUseCaseResponse.Result -> call.respond(loginResponse.token)
            }
        }
    }

    get { it: Paths.authForgotpasswordGet ->
        getServiceProvider()?.let { serviceProvider ->
            val verificationStatusResponse =
                getForgotPasswordStatusUseCase.getVerificationStatus(it.address, serviceProvider)
            handleUseCaseResponse(verificationStatusResponse)
        }
    }

    post { it: Paths.authForgotpasswordPost ->
        val requestBody = call.receive(ForgotPasswordStatusPost::class)
        val address = requestBody.address
        val password = requestBody.password
        getServiceProvider()?.let { serviceProvider ->
            val verificationStatusResponse =
                createForgotPasswordStatusUseCase.getVerificationStatus(address, serviceProvider, password)
            handleUseCaseResponse(verificationStatusResponse)
        }
    }

}