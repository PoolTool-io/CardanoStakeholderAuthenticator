package com.pegasus.csas.persistence

import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.firebase.getValue
import com.pegasus.csas.server.model.ServiceProvider
import com.pegasus.csas.server.model.ServiceProviderPost
import kotlinx.coroutines.runBlocking
import somethingToLong
import java.util.*

class ServiceProviderDao(
    val firebaseDb: FirebaseDb
) {

    fun create(serviceProvider: ServiceProviderPost): ServiceProvider {
        val id = UUID.randomUUID().toString()
        val newServiceProvider = ServiceProvider(
            id = id,
            name = serviceProvider.name,
            email = serviceProvider.email,
            apiKey = UUID.randomUUID().toString(),
            createdAt = Date().time,
            stakeholderVerificationFee = serviceProvider.stakeholderVerificationFee,
            poolOwnerVerificationFee = serviceProvider.poolOwnerVerificationFee
        )
        firebaseDb.serviceProviders.child(id).setValueAsync(newServiceProvider)
        return newServiceProvider
    }

    fun update(serviceProvider: ServiceProvider): ServiceProvider {
        val updatedServiceProvider = serviceProvider.copy(
            updatedAt = Date().time,
        )
        firebaseDb.serviceProviders.child(serviceProvider.id).setValueAsync(updatedServiceProvider)
        return updatedServiceProvider
    }

    fun delete(serviceProviderId: String) {
        firebaseDb.serviceProviders.child(serviceProviderId).setValueAsync(null)
    }


    fun all(): List<ServiceProvider> {
        return runBlocking {
            val firebaseProviders = firebaseDb.serviceProviders.getValue().value as? Map<String, Map<String, Any>>
            firebaseProviders?.mapNotNull {
                mapProvider(it.value)
            } ?: emptyList()
        }
    }

    fun getByProviderId(serviceProviderId: String): ServiceProvider? {
        return runBlocking {
            (firebaseDb.serviceProviders.child(serviceProviderId)
                .getValue().value as? Map<String, Map<String, Any>>)?.let { firebaseProvider ->
                mapProvider(firebaseProvider)
            }
        }
    }


    fun getByApiKey(apiKey: String) = all().firstOrNull { it.apiKey == apiKey }

    private fun mapProvider(firebaseProvider: Map<String, Any>) = ServiceProvider(
        id = firebaseProvider["id"]?.toString() ?: "",
        name = firebaseProvider["name"]?.toString() ?: "",
        email = firebaseProvider["email"]?.toString() ?: "",
        apiKey = firebaseProvider["apiKey"]?.toString() ?: "",
        createdAt = firebaseProvider["createdAt"]?.somethingToLong() ?: 0L,
        updatedAt = firebaseProvider["updatedAt"]?.somethingToLong() ?: 0L,
        stakeholderVerificationFee = firebaseProvider["stakeholderVerificationFee"]?.somethingToLong() ?: 0L,
        poolOwnerVerificationFee = firebaseProvider["poolOwnerVerificationFee"]?.somethingToLong() ?: 0L,
    )

}