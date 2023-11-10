package com.pegasus.csas.authenticator

import com.google.gson.annotations.SerializedName
import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.cardanoaddress.PoolRewardAddressInspector
import com.pegasus.csas.cardanoaddress.TransactionTracker
import com.pegasus.csas.dynamodb.DynamoDbHelper
import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.firebase.getValue
import com.pegasus.csas.persistence.VerificationStatusDao
import com.pegasus.csas.server.model.ServiceProvider
import com.pegasus.csas.server.model.VerificationStatus
import com.pegasus.csas.server.model.VerificationStatus.Status.pending
import com.pegasus.csas.server.model.VerificationStatus.Status.verified
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import kotlin.streams.toList

abstract class BaseVerificationStatusUseCase(
    private val verificationStatusDao: VerificationStatusDao,
    private val firebaseDb: FirebaseDb,
    private val cardanoAddress: CardanoAddress,
    private val transactionTracker: TransactionTracker,
    private val onStatusNotFound: (serviceProvider: ServiceProvider, userId: String, stakeKeyHash: String, password: String?) -> VerificationStatusResponse
) {

    private val debugPrinter = DebugPrinter()
    private val poolRewardAddressInspector = PoolRewardAddressInspector()
    private val dynamoDbHelper = DynamoDbHelper()

    open suspend fun getVerificationStatus(
        address: String,
        userId: String,
        serviceProvider: ServiceProvider,
        password: String? = null
    ): VerificationStatusResponse {
        val timestamp = Date().time
        debugPrinter.print(
            timestamp,
            "[BaseVerificationStatusUseCase] - $timestamp - ${getClassName()} getVerificationStatus address: $address userId: $userId password:${
                getHiddenPassword(
                    password
                )
            }"
        )
        var stakeKeyHash = getStakeKeyHash(address)

        when {
            stakeKeyHash == null -> {
                debugPrinter.print(timestamp, "[BaseVerificationStatusUseCase] - $timestamp - Error Invalid address")
                return InvalidRequest("Invalid address")
            }
            userId.isBlank() -> {
                debugPrinter.print(timestamp, "[BaseVerificationStatusUseCase] - $timestamp - Missing userId")
                return InvalidRequest("Missing userId")
            }
            else -> {
                var verificationStatus = verificationStatusDao.get(stakeKeyHash, userId, serviceProvider.id)
                debugPrinter.print(
                    timestamp,
                    "[BaseVerificationStatusUseCase] - $timestamp retrieved verificationStatus: $verificationStatus"
                )
                when {
                    verificationStatus == null -> {
                        debugPrinter.print(timestamp, "[BaseVerificationStatusUseCase] - $timestamp - onStatusNotFound")
                        return onStatusNotFound(serviceProvider, userId, stakeKeyHash, password)
                    }
                    verificationStatus.status == pending -> {
                        val oldVerificationStatus = verificationStatus!!
                        verificationStatus =
                            updateVerificationStatus(oldVerificationStatus, serviceProvider.id, password, timestamp)
                    }
                }
                return Status(verificationStatus!!)
            }
        }
    }

    private fun getClassName(): String {
        return if (this is CreateVerificationStatusUseCase) {
            "POST"
        } else if (this is GetVerificationStatusUseCase) {
            "GET"
        } else {
            "This should not happen $this"
        }
    }

    private fun getHiddenPassword(password: String?): String {
        return if (password == null) {
            "Password is null"
        } else if (password.isBlank()) {
            "Password is blank"
        } else {
            "*******"
        }
    }

    protected suspend fun getStakeKeyHash(address: String): String? {
        val stakeKeyHash = cardanoAddress.inspect(address)?.stakeKeyHash
        if (stakeKeyHash == null) {
            //Maybe stakeKeyHash was provided instead of address
            val isStakeKeyHashProvided = firebaseDb.stakeHistory.child(address).getValue().value != null
            if (isStakeKeyHashProvided) {
                return address
            }
        } else {
            return stakeKeyHash
        }
        return null
    }

    private suspend fun updateVerificationStatus(
        verificationStatus: VerificationStatus,
        providerId: String,
        password: String?,
        timestamp: Long
    ): VerificationStatus {
        var status = verificationStatus.copy()
        debugPrinter.print(timestamp, "[BaseVerificationStatusUseCase] - $timestamp - updateVerificationStatus")
        if (password?.isNotBlank() == true) {
            val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
            status = verificationStatusDao.update(
                status.copy(
                    passwordHash = passwordHash
                ), providerId
            )!!
            debugPrinter.print(
                timestamp,
                "[BaseVerificationStatusUseCase] - $timestamp - password is not blank, updated status in database: $status"
            )
        } else {
            debugPrinter.print(
                timestamp,
                "[BaseVerificationStatusUseCase] - $timestamp - password is blank! ${
                    getHiddenPassword(
                        password
                    )
                }"
            )
        }
        val transactions = transactionTracker.getTransactionsForAddress(status.paymentToAddress)
        debugPrinter.print(
            timestamp,
            "[BaseVerificationStatusUseCase] - $timestamp - Transactions size: ${transactions.size}"
        )
        val lastTransactionFromAddress =
            transactions.parallelStream()
                .filter { it.fromStakeKeyHashArray.contains(status.stakeKeyHash) }
                .toList()
                .maxByOrNull { it.date }

        debugPrinter.print(
            timestamp,
            "[BaseVerificationStatusUseCase] - $timestamp - lastTransactionFromAddress: $lastTransactionFromAddress"
        )

        return if (lastTransactionFromAddress?.value == status.paymentAmount) {
            debugPrinter.print(
                timestamp,
                "[BaseVerificationStatusUseCase] - $timestamp - Verification transaction is found!"
            )

            val user = firebaseDb.usersAuth.child(status.userId).child("passwordHash").getValue().value

            val userAuthMap = mutableMapOf<String, Any>()

            if (user == null) {
                status.passwordHash?.let {
                    userAuthMap["passwordHash"] = it
                }
                userAuthMap["createdAt"] = Date().time
                debugPrinter.print(
                    timestamp,
                    "[BaseVerificationStatusUseCase] - $timestamp - User is null! writing passwordHash and createdAt. userAuthMap: $userAuthMap"
                )
                firebaseDb.verifiedAddresses.child(providerId).push().setValueAsync(status.stakeKeyHash)
            }
            firebaseDb.address2User.child(status.stakeKeyHash).setValueAsync(status.userId)

            //TODO change from verifiedAddresses to privMeta/myAddresses
            val existingVerifiedAddresses = firebaseDb.usersAuth.child(status.userId).child("verifiedAddresses")
                .getValue().value as? MutableMap<String, String> ?: mutableMapOf()
            existingVerifiedAddresses[UUID.randomUUID().toString()] = status.stakeKeyHash
            userAuthMap["verifiedAddresses"] = existingVerifiedAddresses

            debugPrinter.print(
                timestamp,
                "[BaseVerificationStatusUseCase] - $timestamp - Writing userAuthMap to users/auth/${status.userId}. userAuthMap: $userAuthMap"
            )
            firebaseDb.usersAuth.child(status.userId).updateChildrenAsync(userAuthMap)


            //checks if address is a reward address for a pool.
            poolRewardAddressInspector.isRewardAddress(status.stakeKeyHash)?.let { poolId ->
                val userPools = firebaseDb.privMeta.child(status.userId).child("myPools")
                    .getValue().value as? MutableMap<String, Boolean> ?: emptyMap()
                if (!userPools.keys.contains(poolId)) {
                    firebaseDb.privMeta.child(status.userId).child("myPools").child(poolId).setValueAsync(true)

                    val userApiKeys = firebaseDb.privMeta.child(status.userId).child("myApiKeys")
                        .getValue().value as? MutableMap<String, Any> ?: emptyMap()
                    if (userApiKeys.isEmpty()) {
                        val apiKey = UUID.randomUUID().toString()
                        val apiKeyUpdate = ApiKeyUpdate(
                            apiKeyName = "Generated",
                            poolIds = mapOf(poolId to true)
                        )
                        firebaseDb.privMeta.child(status.userId).child("myApiKeys").child(apiKey).setValueAsync(apiKeyUpdate)
                        dynamoDbHelper.insertToApiUserTable(apiKey, status.userId)
                    } else {
                        val apiKey = userApiKeys.keys.first()
                        firebaseDb.privMeta.child(status.userId).child("myApiKeys").child(apiKey).child("poolids").child(poolId).setValueAsync(true)
                    }
                    dynamoDbHelper.insertToApiPoolTable(poolId, status.userId)
                }
            }

            //Add the new address to myAddresses
            firebaseDb.privMeta.child(status.userId).child("myAddresses").child(status.stakeKeyHash).child("nickname").setValueAsync("")

            debugPrinter.print(timestamp, "[BaseVerificationStatusUseCase] - $timestamp - About to update verification")
            verificationStatusDao.update(
                status.copy(
                    status = verified,
                    verificationDate = lastTransactionFromAddress.date.time
                ), providerId
            ).also {
                debugPrinter.print(
                    timestamp,
                    "[BaseVerificationStatusUseCase] - $timestamp - updated verification status: $it"
                )
            }
        } else {
            debugPrinter.print(
                timestamp,
                "[BaseVerificationStatusUseCase] - $timestamp - required transaction not found for verification"
            )
            status
        }
    }

    private data class ApiKeyUpdate(
        @SerializedName("api_key_name") val apiKeyName: String = "Generated",
        @SerializedName("poolids") val poolIds: Map<String, Boolean>
    )
}

sealed class VerificationStatusResponse
object StatusNotFound : VerificationStatusResponse()
class InvalidRequest(val message: String) : VerificationStatusResponse()
class AlreadyRegisteredAddress(val message: String) : VerificationStatusResponse()
class Status(val status: VerificationStatus) : VerificationStatusResponse()