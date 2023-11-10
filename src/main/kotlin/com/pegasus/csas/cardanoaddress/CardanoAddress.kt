package com.pegasus.csas.cardanoaddress

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.pegasus.csas.utils.execute
import com.pegasus.csas.utils.serviceConfig

class CardanoAddress {

    private val gson = Gson()

    fun inspect(address: String): AddressInspectionResult? {
        return try {
            val command = listOf(
                "/bin/sh",
                "-c",
                "echo $address | $CARDANO_ADDRESS_INSPECT").toTypedArray()

            val response = execute(command)
            gson.fromJson(response, AddressInspectionResult::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            println("CardanoAddress failed to inspect address $address")
            null
        }
    }

    companion object {
        private val CARDANO_ADDRESS_INSPECT = "${serviceConfig.cardanoAddressPath} address inspect"
    }

}

data class AddressInspectionResult(
    @SerializedName("stake_reference") val stakeReference: String,
    @SerializedName("stake_key_hash") val stakeKeyHash: String,
    @SerializedName("address_style") val style: String,
    @SerializedName("spending_key_hash") val spendingKeyHash: String
)
