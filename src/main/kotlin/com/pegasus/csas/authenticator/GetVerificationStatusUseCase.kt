package com.pegasus.csas.authenticator

import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.cardanoaddress.TransactionTracker
import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.persistence.VerificationStatusDao

class GetVerificationStatusUseCase(
    verificationStatusDao: VerificationStatusDao,
    firebaseDb: FirebaseDb,
    cardanoAddress: CardanoAddress,
    transactionTracker: TransactionTracker
) : BaseVerificationStatusUseCase(
    verificationStatusDao,
    firebaseDb,
    cardanoAddress,
    transactionTracker,
    { _, _, _, _ ->
        StatusNotFound
    }
)
