DCSA EBL Envelope
=================================================================================================================================================

# eB/L interoperability technical specifications
This document provides the technical description and specifications for transferring eBL documents between platforms using the eBL Envelope.
General concepts and backgrounds can be found [here](CONCEPTS_AND_BACKGROUND.md).
The rendered Open API specification for the is available on the [DCSA Swaggerhub](https://app.swaggerhub.com/apis/dcsaorg/DCSA_EEC/0.12-alpha).

## Open points for discussion

- [ ] Provide supporting documents via separate download.
- [ ] Title transfer - which actions are relevant?

## Contributing
See [the contributing guide](CONTRIBUTING.md) for detailed instructions on how to get started with our project.

# Transferring BL control to another platform
Transferring the BL control of possession is accomplished by sending a _**"Transferblock"**_ from the sending platform to the receiving platform.

## Overview of a `transferblock`
A `transferblock` is a single JSON structure described [here](https://app.swaggerhub.com/apis/dcsaorg/DCSA_EEC/0.12-alpha#/transferblock).
The `transferblock` has the following structure:
* data of the B/L according to the [DCSA transportDocument specification](https://app.swaggerhub.com/domains/dcsaorg/DOCUMENTATION_DOMAIN/2.1.0#/components/schemas/transportDocument)
* The complete endorsement chain as signed eblEnvelopes transferred between platforms
  * signature of the eblEnvelope - signed with the private key of the sending platform
  * sha256 hash of the eblEnvelope
  * eblEnvelope
    * sha256 hash of the previous eblEnvelope
    * sha256 hash of the B/L data
    * list of transactions that are exported and shared between platforms

Which is depicted in this diagram:
![transferblock structure](specifications/transferblock-structure.png)

### EBL envelope
Information regarding the B/L control of possession is transferred into the EBL envelope. This envelope is signed by the sending platform.

The EBL envelope contains:
* the list of transactions that are transfered to another platform
* the hash of the B/L information (`transportdocument`)
* the hash of the previous ebl envelope (null if this is the first cross-platform transfer)

the EBL envelope itself is signed by the sending platform.
This **_signature_** and the **_hash of the EBL envelope_** are transferred alongside with the  EBL envelope so the receiving party can verify the received EBL envelope.

#### Transactions
A transaction inside the eblEnvelope are the representation of the transfer of control of possession.
Each transaction inside the transactions (list) of the EBL envelope consists of:
* **action** - action for processing the transaction such as `TRNS` (Transfer of possession). Please see the swagger spec for the full list.
* **comments** - free text comment for the party receiving the transaction
* **timestamp** - Unix epoch with millisecond precision of when the transaction was created
* **isToOrder** - indicator if the B/L is to order
* **platformHost** - base url of the platform which created the transaction
* **actor** - Identity information about the party who performed the action.
* **recipient** - Identity information about the party who is the recipient for the action (where applicable). See [Identity below](README.md#identity)

This results in the following JSON structure:
```json
{
  "endorsementChain": [
    {
      "envelopeHash": "20a0257b313ae08417e07f6555c4ec829a512c083f3ead16b41158018a22abe9",
      "signature": "eyJhbGciOiJSUzI1NiIsImtpZCI6IlVhRVdLNmt2ZkRITzNZT2NwUGl2M1RCT2JQTzk2SFZhR2U0czFhUUxBZU0ifQ.eyJkb2N1bWVudEhhc2giOiJmZDg2OGM4MmU5OTc3N2I0NzJhMTY3NzM5MGQ5NTRkYmIwMTMxY2IzYjBmNTVjOGVmNTE5Njk4NTY0MTBkMzhlIiwicHJldmlvdXNFbnZlbG9wZUhhc2giOm51bGwsInRyYW5zYWN0aW9ucyI6W3siaW5zdHJ1Y3Rpb24iOiJJU1NVIiwiY29tbWVudHMiOiJUaGUgQi9MIGhhcyBiZWVuIGlzc3VlZC4iLCJ0aW1lc3RhbXAiOjE2NTgzODUxNjYzMDI0NDIyMDAsImlzVG9PcmRlciI6dHJ1ZSwicGxhdGZvcm1Ib3N0IjoibG9jYWxob3N0Ojg0NDMiLCJ0cmFuc2ZlcmVlIjoiNDM1NDk4NTAyNDhAbG9jYWxob3N0Ojg0NDMifV19.aybAB3RUg_UM2WcvoE4s807Kf7BhSALZq1EvF9f_AUP6ZZgOP4cFS0rlLHSVrobKlF_Og-w0K_M9SPAQS6UnY0hht6pwHTHmoxPmWoQ-ARhnsThjhB3ZYhDbrroJnYPkQAjCmKXPrhNi2z9Fn4GvaI6iRjfJMQchSWQtAEjhqSLNaHtRwec65CDYEZ6OTHX7uP5g3WZtCfmswRGrkcCLosveSxsFpezjjOfTEo2NCCtO0tAxtNtus4GsF8QIMrM6QknJ4909ZrVonvtMUoRffFoUqgemfaFggK5XriLgn2OdSn8ZBhRn_ZqVojuf26mXrVm12C9jSQPPfPGy-myrOQ"
    }
  ]
}
```

## Process of transferring

Transferring a B/L between platforms consists of creating and signing the ebl Envelope and wrap it in a `transferblock`.
This `transferblock` is being sent via an HTTP PUT to the receiving platform. Which is depicted in this diagram:

![cross platform B/L transfer](specifications/plantuml/transfer-sequence-diagram.png)

### Response message
The transfer operation: PUT `/v1/transferblock` is a synchronous operation. The transactional integrity is maintained by the client.

In order to verify the transfer has been successfully processed and the integrity of the message is maintained the response message is signed by the receiving party and verified by the sending party. The signed response enables the sender to confirm this is a final response (accept or reject of the transfer).
For more information about both the request and response signatures see the [Signatures](README.md#signatures) paragraph below

## Identity
In order to transfer the B/L to another platform identity information of the receiving party, the transferee, must be known to the sending platform.
The exchange of identities between users of platforms is out of scope for this reference implementation.
Next to exchanging the identities, the identities must be formatted in a manner understandable and sharable between various platforms.

All transferee identity information is exchanged with an **_eBLPlatformIdentifier_** in combination with the [DCSA Party object](https://app.swaggerhub.com/domains/dcsaorg/DOCUMENTATION_DOMAIN/2.0.1#/components/schemas/party).

### eBLPlatformIdentifier
The eBLPlatformIdentifier is a combination of the transferee user identification _on the local platform_ and the platform.
EBLPlatformIdentifiers are formatted similar to email addresses: _**localid@platformdomain.ext**_ for example: **_gV2ZDy0jmae7@dcsaebplatform.org_**
In this example **_gV2ZDy0jmae7_** is the local identifier on the fictitious **_dcsaebplatform.org_**

### Additional identity information
While the eBLPlatformIdentifier is required, it is optional but recommended for providing additional identity information regarding the transferee.
This additional information can be used by the receiving platform to perform additional verification. For providing this additional information the DCSA Party object is used.
The DCSA Party object allows of the provision of LEI, tax reference and DID and many others documented in the [DCSA Party object](https://app.swaggerhub.com/domains/dcsaorg/DOCUMENTATION_DOMAIN/2.0.1#/components/schemas/party).

### Service discovery
The platform domain part of the eBLPlatformIdentifier being a resolvable domain name can be used for DNS based service discovery.
With this an additional TXT record can be created linking to the API endpoint providing the PUT `/v1/transferblock` operation.

Example:
dcsaeblplatform.org can link to a TXT record containing the full API endpoint -> `api.dcsaeblplatform.org/v1/transferblocks`

## Security considerations

### Transport level security
Security on the transport level requires:
* mTLS
* TLSv1.2 or TLSv1.3
* When using TLSv1.2 only Cipher suites that support [perfect forward secrecy](https://datatracker.ietf.org/doc/html/rfc5489.html)

TLSv1.3 mandates perfect forward secrecy, when using TLSv1.2 this means the usage of the following cipher suites:
* TLS_ECDHE_RSA_WITH_AES256_GCM_SHA384
* TLS_ECDHE_RSA_WITH_AES128_GCM_SHA256

### Authentication
Authentication between platforms require:
* OIDC
* Client credentials grant flow

### Signatures
The signatures used in the transfer of the B/L across platforms have the following characteristics:
for the request message:
* signature covers the eblEnvelope json structure
* signature is created using the private key of the sending platform
* the signature is transfered as a JWS (Json Web Signature as described in [RFC 7515](https://datatracker.ietf.org/doc/html/rfc7515)) in the **_signature_** field
* the JWS is transfered in the compact serialization format (as described in [Section 7 of RFC 7515](https://datatracker.ietf.org/doc/html/rfc7515#section-7.1))
* the signature algorithms must be `PS256` or `ES256`. Algorithms that are too weak include `RS256` and `none`.


For the response message:
* signature covers the envelopeHash of the last item in the endorsement chain
* signature is created using the private key of the receiving platform
* the signature is transfered as a JWS (Json Web Signature as described in [RFC 7515](https://datatracker.ietf.org/doc/html/rfc7515)) response body
* the JWS is transfered in the compact serialization format (as described in [Section 7 of RFC 7515](https://datatracker.ietf.org/doc/html/rfc7515#section-7.1))

### Additional specifications and requirements around security
Digital certificate standards are a balance between information security, ease of adoption, and cost. To sign an eBL envelope with a private key of a digital certificate, the following technical minimum requirements MUST be met to guarantee trustworthy eBL envelopes:

| Category                                          | Standard                                                             |
|---------------------------------------------------|----------------------------------------------------------------------|
| Certificate format                                | X.509 (<https://www.itu.int/rec/T-REC-X.509>)                        |
| Minimum requirements signature algorithm          | `SHA256withRSA` or `SHA256withECDSA`                                 |
| Minimum requirements for OIDC signature algorithm | `PS256` or `ES256`                                                   |
| Minimum key length for asymmetric keys            | 2048-bit for RSA or 256-bit for elliptic curve algorithm             |
| Minimum key length for symmetric keys             | 128-bit, though 256-bit is recommended where possible                |
| Certificate revocation information method         | OCSP (IETF RFC 6960)                                                 |
| Certificate validity period                       | Maximum of 2 years (validity MUST be part of the certificate)        |
| Signature format                                  | See above in the **Signatures** section                              |
| Archive period (for certificates & signatures)    | Minimum of 10 Years for both sender and receiver of signed documents |

### Recommended enhancements

The following are a list of enhancements that implementors are recommended to consider:

 * OAuth2 authentication should be done via the `private_key_jwt` Client Authentication method per [OIDC spec on Client Authentication](https://openid.net/specs/openid-connect-core-1_0.html#ClientAuthentication) or use `tls_client_auth` as defined in [Mutual TLS for OAuth Client Authentication RFC 8705](https://www.rfc-editor.org/rfc/rfc8705).
 * Implement `DNSSEC` optionally with a `CAA` record to pin the Certificate Authority used by your organization.
 * Support the `x-fapi-interaction-id` transaction header described in [OIDC FAPI v1 spec](https://openid.net/specs/openid-financial-api-part-1-1_0.html) to have a common transaction ID for HTTP requests.

