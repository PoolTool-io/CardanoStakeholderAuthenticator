package com.pegasus.csas.authenticator

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.annotations.SerializedName
import com.pegasus.csas.cardanoaddress.CardanoAddress
import com.pegasus.csas.firebase.AuthenticatorFirebaseApp
import com.pegasus.csas.firebase.FirebaseDb
import com.pegasus.csas.firebase.getValue
import org.mindrot.jbcrypt.BCrypt

class LoginUserUseCase(
    private val firebaseDb: FirebaseDb
) {

    private val cardanoAddress = CardanoAddress()

    suspend fun login(address: String, password: String): LoginUserUseCaseResponse {
        return try {
            val stakeKeyHash = getStakeKeyHash(address)
                ?: return LoginUserUseCaseResponse.InvalidAddress

            val userId = firebaseDb.address2User.child(stakeKeyHash).getValue().value as? String
                ?: return LoginUserUseCaseResponse.Unauthenticated

            val savedPassword = firebaseDb.usersAuth.child(userId).child("passwordHash").getValue().value as? String
                ?: return LoginUserUseCaseResponse.Unauthenticated

            return if (BCrypt.checkpw(password, savedPassword)) {
                val token = FirebaseAuth.getInstance(AuthenticatorFirebaseApp.app).createCustomToken(userId)
                LoginUserUseCaseResponse.Result(LoginToken(token))
            } else {
                LoginUserUseCaseResponse.Unauthenticated
            }
        } catch (e: Exception) {
            println("LoginUserUseCase failed!")
            e.printStackTrace()
            LoginUserUseCaseResponse.UnknownError
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

    sealed class LoginUserUseCaseResponse {
        object InvalidAddress : LoginUserUseCaseResponse()
        object Unauthenticated : LoginUserUseCaseResponse()
        object UnknownError : LoginUserUseCaseResponse()
        class Result(val token: LoginToken) : LoginUserUseCaseResponse()
    }

    data class LoginToken(
        @SerializedName("token") val token: String
    )

}


