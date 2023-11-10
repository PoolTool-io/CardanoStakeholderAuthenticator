package com.pegasus.csas.persistence

import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.firebase.getValue
import com.pegasus.csas.server.model.VerificationStatus
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import somethingToLong
import java.util.*

class VerificationStatusDao(
    private val firebaseDb: FirebaseDb
) {

    fun get(stakeKeyHash: String, userId: String, serviceProviderId: String): VerificationStatus? {
        return runBlocking {
            (firebaseDb.verificationStatuses
                .child(serviceProviderId)
                .child(userId)
                .child(stakeKeyHash).getValue().value as? Map<String, Any>)?.let {
                mapStatus(it)
            }
        }
    }

    fun create(verificationStatus: VerificationStatus, providerId: String): VerificationStatus {
        return runBlocking {
            firebaseDb.verificationStatuses
                .child(providerId)
                .child(verificationStatus.userId)
                .child(verificationStatus.stakeKeyHash)
                .setValueAsync(verificationStatus)
            verificationStatus
        }
    }

    fun update(verificationStatus: VerificationStatus, providerId: String): VerificationStatus {
        return runBlocking {
            val existingPasswordHash = firebaseDb.verificationStatuses
                .child(providerId)
                .child(verificationStatus.userId)
                .child(verificationStatus.stakeKeyHash)
                .child("passwordHash").getValue() as? String

            val updatedStatus = verificationStatus.copy(
                passwordHash = verificationStatus.passwordHash?.ifBlank { existingPasswordHash }
            )
            firebaseDb.verificationStatuses
                .child(providerId)
                .child(verificationStatus.userId)
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
        userId = firebaseStatus["userId"]?.toString() ?: "",
        passwordHash = firebaseStatus["passwordHash"]?.toString() ?: "",
    )


}