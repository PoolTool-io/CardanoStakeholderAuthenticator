package com.pegasus.csas.authenticator

import com.google.firebase.database.FirebaseDatabase
import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.firebase.AuthenticatorFirebaseApp
import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.firebase.getValue
import com.pegasus.csas.server.firebaseDb
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import java.util.concurrent.TimeUnit

internal class BaseVerificationStatusUseCaseTest {

    @Test
    fun checkBuggyUsers() {
        while(true) {
            runBlocking {

                val auth = firebaseDb.usersAuth.getValue().value as? Map<String, Map<String, Any>>

                println("Number of users: ${auth?.size}")
                var buggyUser = 0
                var userIds = mutableListOf<String>()
                auth?.forEach {
                    if (it.value["passwordHash"] == null) {
                        val userId = it.key
                        println("Fixing $userId")
                        val verificationStatuses =
                            firebaseDb.verificationStatuses.child("3d37c4e7-3031-475c-95fb-699dd29c030e").child(userId)
                                .getValue().value as Map<String, Map<String, Any>>
                        verificationStatuses.forEach { stakeKeyHash, verificationStatusMap ->
                            if (verificationStatusMap["status"] == "verified") {
                                val passwordHash = verificationStatusMap["passwordHash"]
                                val createdAt = verificationStatusMap["createdDate"]

                                firebaseDb.usersAuth.child(userId).child("passwordHash").setValueAsync(passwordHash)
                                firebaseDb.usersAuth.child(userId).child("createdAt").setValueAsync(createdAt)
                            }
                        }

                        val verifiedAddresses = firebaseDb.usersAuth.child(userId).child("verifiedAddresses")
                            .getValue().value as Map<String, String>
                        val visitedAddress = mutableListOf<String>()
                        val keysToDelete = mutableListOf<String>()
                        verifiedAddresses.forEach { uuid, stakeKeyHash ->
                            if (visitedAddress.contains(stakeKeyHash)) {
                                keysToDelete.add(uuid)
                            }
                            visitedAddress.add(stakeKeyHash)
                        }
                        keysToDelete.forEach {
                            firebaseDb.usersAuth.child(userId).child("verifiedAddresses").child(it).removeValueAsync()
                        }

                        userIds.add(userId)
                        buggyUser++
                    }
                }
                println("Fixed $buggyUser affected users")
                println("userIds: $userIds")
            }
            Thread.sleep(TimeUnit.MINUTES.toMillis(30))
        }
    }

    @Test
    fun passwordHash() {
        val password = "GooglePlay"
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

        println("Password hash: $passwordHash")

    }

    @Test
    fun deleteUser() {
//        val userId = "mobile-app-test-user"
        val userId = "8946857b-8327-4878-be70-c7a85ede62f1"

        delete(userId)
    }

    @Test
    fun getStatusForAddress() {

        val address =
            "addr1q96hcz3x0g65kw7sulxpue6kcfgzryeu08lql73mjn9vn8ykc7hyp4nr6hrccgkqt9dxy53japftq9syt4rpfy22483snu2k4h"

        CardanoAddress().inspect(address)?.stakeKeyHash?.let { stakeKeyHash ->
            runBlocking {
                val db = FirebaseDb()
                (db.verificationStatuses.child("3d37c4e7-3031-475c-95fb-699dd29c030e")
                    .getValue().value as Map<String, Map<String, Any>>).filter {
                    it.value.keys.contains(stakeKeyHash)
                }.forEach {
                    println("Result: $it")
                }
            }
        } ?: throw IllegalStateException("Invalid address")
    }

    @Test
    fun getStatusForStakeKeyHash() {
        val stakeKeyHash =
            "da89572feefd949b0bb160a659408f5665841500f13134132b78e4c7"

        runBlocking {
            val db = FirebaseDb()
            (db.verificationStatuses.child("3d37c4e7-3031-475c-95fb-699dd29c030e")
                .getValue().value as Map<String, Map<String, Any>>).filter {
                it.value.keys.contains(stakeKeyHash)
            }.forEach {
                println("Result: $it")
            }
        }

    }

    private fun delete(userId: String) {
        val userAddresses = mutableListOf<String>()
        runBlocking {
            (firebaseDb.address2User.getValue().value as Map<String, String>).forEach {
                val address = it.key
                val addressUserId = it.value
                if (userId == addressUserId) {
                    userAddresses.add(address)
                    firebaseDb.address2User.child(address).setValueAsync(null)
                }
            }

            FirebaseDatabase.getInstance(AuthenticatorFirebaseApp.app)
                .getReference("Mainnet/users/verification_statuses/3d37c4e7-3031-475c-95fb-699dd29c030e").child(userId)
                .setValueAsync(null)

            firebaseDb.usersAuth.child(userId).setValueAsync(null)
            FirebaseDatabase.getInstance(AuthenticatorFirebaseApp.app).getReference("Mainnet/users/privMeta")
                .child(userId).setValueAsync(null)
            FirebaseDatabase.getInstance(AuthenticatorFirebaseApp.app).getReference("Mainnet/users/pubMeta")
                .child(userId).setValueAsync(null)

            val allVerifiedAddresses = FirebaseDatabase.getInstance(AuthenticatorFirebaseApp.app)
                .getReference("Mainnet/users/verified_addresses/3d37c4e7-3031-475c-95fb-699dd29c030e")
                .getValue().value as Map<String, String>
            allVerifiedAddresses.forEach {
                if (userAddresses.contains(it.value)) {
                    FirebaseDatabase.getInstance(AuthenticatorFirebaseApp.app)
                        .getReference("Mainnet/users/verified_addresses/3d37c4e7-3031-475c-95fb-699dd29c030e")
                        .child(it.key).setValueAsync(null)
                }
            }
        }
    }
}
