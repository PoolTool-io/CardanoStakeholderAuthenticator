/**
* Cardano Stakeholder Authenticator Service
* TBA
*
* OpenAPI spec version: 1.0.0
* Contact: contact@pegasuspool.info
*
* NOTE: This class is auto generated by the swagger code generator program.
* https://github.com/swagger-api/swagger-codegen.git
* Do not edit the class manually.
*/
package com.pegasus.csas.server.model

/**
 * 
 * @param status 
 * @param createdDate Date of the submission of the verification request. Populated when status is pending or verified. Milliseconds since NTP epoch
 * @param ownerOfPool The poolId of the owner if the stake address declared as pool owner. May be populated when status is pending or verified.
 * @param paymentToAddress The address to which the transaction must be sent to verify the stakeholder. Populated when status is pending or verified.
 * @param paymentAmount The amount to be used to verify this address in lovelaces. Populated when status is pending or verified.
 * @param verificationDate Date of the verification. Populated when status is verified. Milliseconds since NTP epoch
 * @param stakeKeyHash The stakeKeyHash for the requested address to be verified.
 * @param userId The userId from the service provider's system. This is used to distinguish address verification requests from different users.
 * @param passwordHash The hash of the password used when the verification of the address was requested. Used to create the new user after successful verification.
 */
data class VerificationStatus (
    val status: Status,
    val createdDate: Long,
    val ownerOfPool: String? = null,
    val paymentToAddress: String,
    val paymentAmount: Long,
    val verificationDate: Long? = null,
    val stakeKeyHash: String,
    val userId: String,
    val passwordHash: String? = null
) {

    /**
    * 
    * Values: unknown,pending,verified
    */
    enum class Status(val value: String){
    
        pending("pending"),
    
        verified("verified");
    
    }

}
