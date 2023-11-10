/**
 * Cardano Stakeholder Authenticator Service
 * A service to verify stakeholder ownership and provide stake history for addresses
 *
 * OpenAPI spec version: 0.0.2
 * Contact: contact@pegasuspool.info
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */
package com.pegasus.csas.server.model


/**
 * 
 * @param id The service provider's ID
 * @param name The name of the service provider
 * @param email The email address of the service provider
 * @param apiKey The API key of the service provider
 * @param createdAt Date of the creation of the service provider entity. Milliseconds since NTP epoch
 * @param updatedAt Last updated date of the service provider entity. Milliseconds since NTP epoch
 * @param stakeholderVerificationFee The expected amount to be sent by the stakeholders to verify their ownership (lovelaces)
 * @param poolOwnerVerificationFee The expected amount to be sent by the pool owner to verify their ownership (lovelaces)
 */
data class ServiceProvider (
    val id: String,
    val name: String,
    val email: String,
    val apiKey: String,
    val createdAt: Long,
    val stakeholderVerificationFee: kotlin.Long,
    val poolOwnerVerificationFee: kotlin.Long,
    val updatedAt: Long? = null
) {
}