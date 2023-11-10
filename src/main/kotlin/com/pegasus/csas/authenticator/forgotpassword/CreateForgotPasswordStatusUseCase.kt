package com.pegasus.csas.authenticator.forgotpassword

import com.pegasus.csas.authenticator.InvalidRequest
import com.pegasus.csas.authenticator.Status
import com.pegasus.csas.authenticator.VerificationStatusResponse
import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.cardanoaddress.TransactionTracker
import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.persistence.ForgotPasswordStatusDao
import com.pegasus.csas.server.model.ServiceProvider
import com.pegasus.csas.server.model.VerificationStatus
import com.pegasus.csas.utils.serviceConfig
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class CreateForgotPasswordStatusUseCase(
    private val forgotPasswordStatusDao: ForgotPasswordStatusDao,
    firebaseDb: FirebaseDb,
    cardanoAddress: CardanoAddress,
    transactionTracker: TransactionTracker
) : BaseForgotPasswordUseCase(
    forgotPasswordStatusDao,
    firebaseDb,
    cardanoAddress,
    transactionTracker,
    true,
    { serviceProvider, stakeKeyHash, password ->
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        val status = forgotPasswordStatusDao.create(
            buildNewVerificationStatus(serviceProvider, stakeKeyHash, passwordHash),
            serviceProvider.id
        )

        Status(status)
    }
) {

    override suspend fun getVerificationStatus(
        address: String,
        serviceProvider: ServiceProvider,
        password: String?
    ): VerificationStatusResponse {
        if(password.isNullOrEmpty()) {
            return InvalidRequest("Missing password")
        }
        return super.getVerificationStatus(address, serviceProvider, password)
    }

}

private fun buildNewVerificationStatus(serviceProvider: ServiceProvider, stakeKeyHash: String, passwordHash: String) = VerificationStatus(
    status = VerificationStatus.Status.pending,
    createdDate = Date().time,
    paymentToAddress = serviceConfig.forgotPasswordPaymentAddress,
    paymentAmount = 1_000_000L + ((1..1000).random() * 1000),
    stakeKeyHash = stakeKeyHash,
    userId = "",
    passwordHash = passwordHash
)
