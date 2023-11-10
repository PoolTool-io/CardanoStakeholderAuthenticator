package com.pegasus.csas.utils

import com.google.gson.Gson
import java.io.File

val serviceConfig: ServiceConfig by lazy {
    System.getProperty("service-config-path")?.let { configPath ->
        val configFile = File(configPath)
        if (!configFile.exists()) {
            throw IllegalStateException("service-config-file does not exist at $configPath")
        }
        try {
            Gson().fromJson(configFile.readText(), ServiceConfig::class.java)
        } catch (e: Exception) {
            throw IllegalStateException("Could not parse service-config-file: ${e.message}.")
        }
    } ?: throw IllegalStateException("service-config-path is not defined!")

}

data class ServiceConfig(
    val destinationAddresses: List<String>,
    val forgotPasswordPaymentAddress: String,
    val cardanoAddressPath: String,
    val dbPath: String,
    val dbUser: String,
    val dbPassword: String

)