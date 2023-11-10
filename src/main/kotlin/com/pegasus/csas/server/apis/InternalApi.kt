package com.pegasus.csas.server.apis

import com.pegasus.csas.server.Paths
import com.pegasus.csas.server.model.ServiceProvider
import com.pegasus.csas.server.model.ServiceProviderPost
import com.pegasus.csas.server.model.ServiceProviderPut
import com.pegasus.csas.server.serviceProvidersDao
import getAdmin
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import java.util.*

@KtorExperimentalLocationsAPI
fun Route.InternalApi() {
    get { it: Paths.internalServiceprovidersGet ->
        getAdmin()?.run {
            val providers = serviceProvidersDao.all()
            call.respond(providers)
        }
    }

    post { it: Paths.internalServiceprovidersPost ->
        getAdmin()?.run {
            try {
                val provider = serviceProvidersDao.create(call.receive(ServiceProviderPost::class))
                call.respond(provider)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }

    put { it: Paths.internalServiceprovidersPut ->

        getAdmin()?.run {

            val provider = call.receive(ServiceProviderPut::class)

            val updatedProvider = serviceProvidersDao.update(ServiceProvider(
                id = provider.id,
                name = provider.name,
                email = provider.email,
                stakeholderVerificationFee = provider.stakeholderVerificationFee,
                poolOwnerVerificationFee = provider.poolOwnerVerificationFee,
                createdAt = Date().time,
                apiKey = provider.apiKey
            ))

            if(updatedProvider != null) {
                call.respond(updatedProvider)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }

    delete { it: Paths.internalServiceprovidersDelete ->
        getAdmin()?.run {
            serviceProvidersDao.delete(it.serviceProviderId)
            call.respond(HttpStatusCode.NoContent)
        }
    }

}

private fun stubServiceProvider() = ServiceProvider(
    id = "234431",
    name = "pooltool",
    email = "contact@pooltool.io",
    apiKey = "e77c9f9f-1d25-41da-85f2-fc3d57fd8a7f",
    createdAt = 1611208105355L,
    updatedAt = 1711208105355L,
    stakeholderVerificationFee = 10_000_000L,
    poolOwnerVerificationFee = 200_000_000L
)