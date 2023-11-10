package com.pegasus.csas.authenticator.forgotpassword

import com.pegasus.csas.authenticator.StatusNotFound
import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.cardanoaddress.TransactionTracker
import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.persistence.ForgotPasswordStatusDao

class GetForgotPasswordStatusUseCase(
    forgotPasswordStatusDao: ForgotPasswordStatusDao,
    firebaseDb: FirebaseDb,
    cardanoAddress: CardanoAddress,
    transactionTracker: TransactionTracker
) : BaseForgotPasswordUseCase(
    forgotPasswordStatusDao,
    firebaseDb,
    cardanoAddress,
    transactionTracker,
    false,
    { _, _, _ ->
        StatusNotFound
    }
)
