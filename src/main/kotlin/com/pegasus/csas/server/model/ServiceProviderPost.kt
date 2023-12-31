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
 * @param name The name of the service provider
 * @param email The email address of the service provider
 * @param stakeholderVerificationFee The expected amount to be sent by the stakeholders to verify their ownership (lovelaces)
 * @param poolOwnerVerificationFee The expected amount to be sent by the pool owner to verify their ownership (lovelaces)
 */
data class ServiceProviderPost (
    /* The name of the service provider */
    val name: String,
    /* The email address of the service provider */
    val email: String,
    /* The expected amount to be sent by the stakeholders to verify their ownership (lovelaces) */
    val stakeholderVerificationFee: Long,
    /* The expected amount to be sent by the pool owner to verify their ownership (lovelaces) */
    val poolOwnerVerificationFee: Long
)