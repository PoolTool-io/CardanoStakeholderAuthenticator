package com.pegasus.csas.persistence

import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.firebase.getValue
import com.pegasus.csas.server.model.VerificationStatus
import kotlinx.coroutines.runBlocking
import somethingToLong

class ForgotPasswordStatusDao(
    private val firebaseDb: FirebaseDb
) {

    fun get(stakeKeyHash: String, serviceProviderId: String): VerificationStatus? {
        return runBlocking {
            (firebaseDb.forgotPasswordStatuses
                .child(serviceProviderId)
                .child(stakeKeyHash).getValue().value as? Map<String, Any>)?.let {
                mapStatus(it)
            }
        }
    }

    fun create(verificationStatus: VerificationStatus, providerId: String): VerificationStatus {
        return runBlocking {
            firebaseDb.forgotPasswordStatuses
                .child(providerId)
                .child(verificationStatus.stakeKeyHash)
                .setValueAsync(verificationStatus)
            verificationStatus
        }
    }

    fun update(verificationStatus: VerificationStatus, providerId: String): VerificationStatus {
        return runBlocking {
            val existingPasswordHash = firebaseDb.forgotPasswordStatuses
                .child(providerId)
                .child(verificationStatus.stakeKeyHash)
                .child("passwordHash").getValue() as? String

            val updatedStatus = verificationStatus.copy(
                passwordHash = verificationStatus.passwordHash ?: existingPasswordHash
            )
            firebaseDb.forgotPasswordStatuses
                .child(providerId)
                .child(verificationStatus.stakeKeyHash)
                .setValueAsync(updatedStatus)
            updatedStatus
        }
    }

    private fun mapStatus(firebaseStatus: Map<String, Any>) = VerificationStatus(
        status = VerificationStatus.Status.valueOf(firebaseStatus["status"]?.toString() ?: ""),
        createdDate = firebaseStatus["createdDate"]?.somethingToLong() ?: 0L,
        paymentToAddress = firebaseStatus["paymentToAddress"]?.toString() ?: "",
        paymentAmount = firebaseStatus["paymentAmount"]?.somethingToLong() ?: 0L,
        verificationDate = firebaseStatus["verificationDate"]?.somethingToLong() ?: 0L,
        stakeKeyHash = firebaseStatus["stakeKeyHash"]?.toString() ?: "",
        userId = "",
        passwordHash = firebaseStatus["passwordHash"]?.toString() ?: "",
    )


}