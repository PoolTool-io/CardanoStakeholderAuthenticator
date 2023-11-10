package com.pegasus.csas.authenticator.forgotpassword

import com.pegasus.csas.authenticator.InvalidRequest
import com.pegasus.csas.authenticator.Status
import com.pegasus.csas.authenticator.VerificationStatusResponse
import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.cardanoaddress.TransactionTracker
import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.firebase.getValue
import com.pegasus.csas.persistence.ForgotPasswordStatusDao
import com.pegasus.csas.server.model.ServiceProvider
import com.pegasus.csas.server.model.VerificationStatus
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

abstract class BaseForgotPasswordUseCase(
    private val forgotPasswordStatusDao: ForgotPasswordStatusDao,
    private val firebaseDb: FirebaseDb,
    private val cardanoAddress: CardanoAddress,
    private val transactionTracker: TransactionTracker,
    private val createNewStatusWhenFound: Boolean,
    private val onStatusNotFound: (serviceProvider: ServiceProvider, stakeKeyHash: String, password: String?) -> VerificationStatusResponse
) {

    open suspend fun getVerificationStatus(
        address: String,
        serviceProvider: ServiceProvider,
        password: String? = null
    ): VerificationStatusResponse {
        val stakeKeyHash = getStakeKeyHash(address)
        when {
            stakeKeyHash == null -> {
                return InvalidRequest("Invalid address")
            }
            else -> {
                var verificationStatus = forgotPasswordStatusDao.get(stakeKeyHash, serviceProvider.id)

                if(verificationStatus != null && createNewStatusWhenFound) {
                    return onStatusNotFound(serviceProvider, stakeKeyHash, password)
                }

                when {
                    verificationStatus == null -> {
                        return onStatusNotFound(serviceProvider, stakeKeyHash, password)
                    }
                    verificationStatus.status == VerificationStatus.Status.pending -> {
                        verificationStatus =
                            updateForgotPasswordStatus(verificationStatus, serviceProvider.id, password)
                    }
                }
                return Status(verificationStatus!!)
            }
        }
    }

    private suspend fun getStakeKeyHash(address: String): String? {
        val stakeKeyHash = cardanoAddress.inspect(address)?.stakeKeyHash
        if(stakeKeyHash == null) {
            //Maybe stakeKeyHash was provided instead of address
            val isStakeKeyHashProvided = firebaseDb.stakeHistory.child(address).getValue().value != null
            if(isStakeKeyHashProvided) {
                return address
            }
        } else {
            return stakeKeyHash
        }
        return null
    }

    private suspend fun updateForgotPasswordStatus(
        verificationStatus: VerificationStatus,
        providerId: String,
        password: String?
    ): VerificationStatus {
        var status = verificationStatus.copy()
        if (password != null) {
            val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
            status = forgotPasswordStatusDao.update(
                status.copy(
                    passwordHash = passwordHash
                ), providerId
            )!!
        }
        val transactions = transactionTracker.getTransactionsForAddress(status.paymentToAddress)

        val lastTransactionFromAddress =
            transactions.filter { it.fromStakeKeyHashArray.contains(status.stakeKeyHash) }
                .maxByOrNull { it.date }

        return if (lastTransactionFromAddress != null && lastTransactionFromAddress.value == status.paymentAmount) {

            val users = firebaseDb.usersAuth.getValue().value as? Map<String, Map<String, Any>>
            users?.forEach {
                val userId = it.key
                val verifiedAddresses = it.value["verifiedAddresses"] as? Map<String, String>
                verifiedAddresses?.forEach {
                    if (it.value == status.stakeKeyHash) {
                        firebaseDb.usersAuth.child(userId).child("passwordHash").setValueAsync(status.passwordHash)
                    }
                }
            }

            forgotPasswordStatusDao.update(
                status.copy(
                    status = VerificationStatus.Status.verified,
                    verificationDate = lastTransactionFromAddress.date.time
                ), providerId
            )
        } else {
            status
        }
    }
}