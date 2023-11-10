package com.pegasus.csas.authenticator

import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.cardanoaddress.TransactionTracker
import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.firebase.getValue
import com.pegasus.csas.persistence.VerificationStatusDao
import com.pegasus.csas.server.model.ServiceProvider
import com.pegasus.csas.server.model.VerificationStatus
import com.pegasus.csas.utils.serviceConfig
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class CreateVerificationStatusUseCase(
    private val verificationStatusDao: VerificationStatusDao,
    private val firebaseDb: FirebaseDb,
    private val cardanoAddress: CardanoAddress,
    transactionTracker: TransactionTracker
) : BaseVerificationStatusUseCase(
    verificationStatusDao,
    firebaseDb,
    cardanoAddress,
    transactionTracker,
    { serviceProvider, userId, stakeKeyHash, password ->
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        val status = verificationStatusDao.create(
            buildNewVerificationStatus(serviceProvider, userId, stakeKeyHash, passwordHash),
            serviceProvider.id
        )

        Status(status)
    }
) {

    override suspend fun getVerificationStatus(
        address: String,
        userId: String,
        serviceProvider: ServiceProvider,
        password: String?
    ): VerificationStatusResponse {
        val existingUserId = firebaseDb.usersAuth.child(userId).getValue().value
        if (existingUserId == null && password.isNullOrEmpty()) {
            return InvalidRequest("Missing password")
        }

        getStakeKeyHash(address)?.let { stakeKeyHash ->
            (firebaseDb.verifiedAddresses.child(serviceProvider.id).getValue().value as? Map<String, String>)?.let { verifiedAddresses ->
                if(verifiedAddresses.containsValue(stakeKeyHash)) {
                    return AlreadyRegisteredAddress("This address is already registered")
                }
            }
        }

        return super.getVerificationStatus(address, userId, serviceProvider, password)
    }

}

private fun buildNewVerificationStatus(
    serviceProvider: ServiceProvider,
    userId: String,
    stakeKeyHash: String,
    passwordHash: String
) = VerificationStatus(
    status = VerificationStatus.Status.pending,
    createdDate = Date().time,
    paymentToAddress = serviceConfig.destinationAddresses.random(),
    paymentAmount = serviceProvider.stakeholderVerificationFee + ((1..1000).random() * 1000),
    stakeKeyHash = stakeKeyHash,
    userId = userId,
    passwordHash = passwordHash
)
