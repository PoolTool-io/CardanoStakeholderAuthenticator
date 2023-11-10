package com.pegasus.csas.server

import com.pegasus.csas.server.model.ServiceProviderPost
import com.pegasus.csas.server.model.ServiceProviderPut
import io.ktor.locations.*

object Paths {

    @Location("/auth/login")
    class authLoginPost()

    @Location("/auth/forgotpassword")
    class authForgotpasswordPost()

    @Location("/auth/forgotpassword/{address}")
    class authForgotpasswordGet(val address: String)

    /**
     *
     * Get the stake history for a given address
     *
     * @param address The address on which the operation will be performed. This can be any of the followings:
     *      - Payment address associated with the wallet (e.g addr1qyhrwn3retgf4n6e8cm9exw3pjmpghk4h4pwflfjzs8v3qwz6dlxpqhca3freqtejk23yvmn4xcmayjvhd6h2lq388sq2250rn)
     *      - Bech32 payment address (e.g.012e374e23cad09acf593e365c99d10cb6145ed5bd42e4fd32140ec881c2d37e6082f8ec523c81799595123373a9b1be924cbb75757c1139e0)
     *      - Stake address (.e.g stake1uy5v9ndlnn3c39arguskgujz0mqkdx3299s68ku3u6le72ch7e76j)
     */
    @Location("/stakehistory/{address}")
    class stakehistoryAddressGet(val address: String)

    /**
     *
     * Get the verification status for a given address
     *
     * @param address The address on which the operation will be performed. This can be any of the followings:
     *      - Payment address associated with the wallet (e.g addr1qyhrwn3retgf4n6e8cm9exw3pjmpghk4h4pwflfjzs8v3qwz6dlxpqhca3freqtejk23yvmn4xcmayjvhd6h2lq388sq2250rn)
     *      - Bech32 payment address (e.g.012e374e23cad09acf593e365c99d10cb6145ed5bd42e4fd32140ec881c2d37e6082f8ec523c81799595123373a9b1be924cbb75757c1139e0)
     *      - Stake address (.e.g stake1uy5v9ndlnn3c39arguskgujz0mqkdx3299s68ku3u6le72ch7e76j)
     */
    @Location("/verification/{address}")
    class verificationAddressGet(val address: String)

    /**
     *
     * Verify a given address.
     * This endpoint should be called for an address with “unknown” status. Calling this endpoint on a pending or verified address will just return the current status of the address.
     * @param address The address on which the operation will be performed. This can be any of the followings:
     *      - Payment address associated with the wallet (e.g addr1qyhrwn3retgf4n6e8cm9exw3pjmpghk4h4pwflfjzs8v3qwz6dlxpqhca3freqtejk23yvmn4xcmayjvhd6h2lq388sq2250rn)
     *      - Bech32 payment address (e.g.012e374e23cad09acf593e365c99d10cb6145ed5bd42e4fd32140ec881c2d37e6082f8ec523c81799595123373a9b1be924cbb75757c1139e0)
     *      - Stake address (.e.g stake1uy5v9ndlnn3c39arguskgujz0mqkdx3299s68ku3u6le72ch7e76j)
     */
    @Location("/verification/{address}")
    class verificationAddressPost(val address: String)

    /**
     * Get all service providers.
     */
    @Location("/internal/serviceproviders")
    class internalServiceprovidersGet()

    /**
     * Create a new service provider.
     */
    @Location("/internal/serviceproviders")
    class internalServiceprovidersPost(val serviceProvider: ServiceProviderPost = stubServiceProviderPost())

    /**
     * Update an existing service provider.
     */
    @Location("/internal/serviceproviders")
    class internalServiceprovidersPut(val serviceProvider: ServiceProviderPut = stubServiceProviderPut())

    /**
     * Update an existing service provider.
     */
    @Location("/internal/serviceproviders")
    class internalServiceprovidersDelete(val serviceProviderId: String)

    /**
     *
     * Inspect a give address
     *
     * @param address The address on which the operation will be performed. This can be any of the followings:
     *      - Payment address associated with the wallet (e.g addr1qyhrwn3retgf4n6e8cm9exw3pjmpghk4h4pwflfjzs8v3qwz6dlxpqhca3freqtejk23yvmn4xcmayjvhd6h2lq388sq2250rn)
     */
    @Location("/address/{address}/inspect")
    class inspectAddressGet(val address: String)

    private fun stubServiceProviderPost() = ServiceProviderPost(
        name = "",
        email = "",
        stakeholderVerificationFee = 0L,
        poolOwnerVerificationFee = 0L
    )

    private fun stubServiceProviderPut() = ServiceProviderPut(
        id = "",
        name = "",
        email = "",
        apiKey = "",
        stakeholderVerificationFee = 0L,
        poolOwnerVerificationFee = 0L
    )

}
