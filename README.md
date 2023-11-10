Kotlin Server library for Cardano Stakeholder Authenticator Service

A REST API service to verify stakeholder ownership and provide stake history for addresses

## Requires

* JDK 1.8

## Build

```
./gradlew check assemble
```

This runs all tests and packages the library.

## Running

The server builds as a fat jar with a main entrypoint. To start the service, run `java --Dservice-config-path=/path/to/service/config jar ./build/libs/cardano-authentication-service.jar`.

<a name="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *https://csas.pegasuspool.info/v1*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AuthenticatorApi* | [**stakehistoryAddressGet**](https://pegasuspool.info/csas/docs/index.html) | **GET** /stakehistory/{address} | Get the stake history for a given address
*AuthenticatorApi* | [**statusAddressGet**](https://pegasuspool.info/csas/docs/index.html) | **GET** /status/{address} | Get the verification status for a given address
*AuthenticatorApi* | [**verifyAddressDelete**](https://pegasuspool.info/csas/docs/index.html) | **DELETE** /verify/{address} | Reset a given address’s status to unknown
*AuthenticatorApi* | [**verifyAddressPost**](https://pegasuspool.info/csas/docs/index.html) | **POST** /verify/{address} | Verify a given address.


<a name="documentation-for-models"></a>
## Documentation for Models

 - [AddressStatus](https://pegasuspool.info/csas/docs/index.html)
 - [StakeHistory](https://pegasuspool.info/csas/docs/index.html)


<a name="documentation-for-authorization"></a>
## Documentation for Authorization

<a name="APIKeyHeader"></a>
### APIKeyHeader

API endpoints must be called with a valid 'X-API-Key' header.

- **Type**: API key
- **API key parameter name**: X-API-Key
- **Location**: HTTP header

