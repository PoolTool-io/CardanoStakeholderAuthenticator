package com.pegasus.csas.firebase

import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseDb {

    var serviceProviders: DatabaseReference
    var verificationStatuses: DatabaseReference
    var forgotPasswordStatuses: DatabaseReference
    var stakeHistory: DatabaseReference
    var address2User: DatabaseReference
    var usersAuth: DatabaseReference
    var privMeta: DatabaseReference
    var verifiedAddresses: DatabaseReference

    init {
        val database = FirebaseDatabase.getInstance(AuthenticatorFirebaseApp.app)
//        database.setPersistenceEnabled(false)

        serviceProviders = database.getReference("Mainnet/service_providers")
        verificationStatuses = database.getReference("Mainnet/users/verification_statuses")
        forgotPasswordStatuses = database.getReference("Mainnet/users/forgot_password_statuses")
        stakeHistory = database.getReference("Mainnet/stake_hist")
        address2User = database.getReference("Mainnet/users/addr2user")
        usersAuth = database.getReference("Mainnet/users/auth")
        privMeta = database.getReference("Mainnet/users/privMeta")
        verifiedAddresses = database.getReference("Mainnet/users/verified_addresses")
    }

}

suspend fun DatabaseReference.getValue(): DataSnapshot {
    return withContext(Dispatchers.Default) {
        suspendCoroutine { continuation ->
            addListenerForSingleValueEvent(FValueEventListener(
                onDataChange = { continuation.resume(it) },
                onError = { continuation.resumeWithException(it.toException()) }
            ))
        }
    }
}

class FValueEventListener(val onDataChange: (DataSnapshot) -> Unit, val onError: (DatabaseError) -> Unit) :
    ValueEventListener {
    override fun onDataChange(data: DataSnapshot) = onDataChange.invoke(data)
    override fun onCancelled(error: DatabaseError) = onError.invoke(error)
}