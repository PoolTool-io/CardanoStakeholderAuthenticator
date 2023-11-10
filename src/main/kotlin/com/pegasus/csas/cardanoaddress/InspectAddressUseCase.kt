package com.pegasus.csas.cardanoaddress

class InspectAddressUseCase {

    private val cardanoAddress = CardanoAddress()

    fun inspect(address: String): InspectAddressUseCaseResult {
        return when (val result = cardanoAddress.inspect(address)) {
            is AddressInspectionResult -> InspectAddressUseCaseResult.Result(result)
            else -> InspectAddressUseCaseResult.InvalidAddress
        }
    }

}

sealed class InspectAddressUseCaseResult {
    data class Result(val result: AddressInspectionResult) : InspectAddressUseCaseResult()
    object InvalidAddress : InspectAddressUseCaseResult()
}
