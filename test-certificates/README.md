# Test certificates & keys
This directory contains a set of key material to ease a local setup and should not be used for other purposes.

All keys containing in this directory are generated on a local machine and self-signed certificates.

* verification-private-key.pem - the private key of the party sending the transferblock to the test application. This key is used to generate a signature that can be verified by this reference application.
* dcsa-jwk-verify.jks - the JKS containing the public certificate used by the reference implementation to perform a verification of the signature. Contains the public certificate of the keypair of which verification-private-key.pem is the private key.
* dcsa-jwk.jks - contains the private key and public key and certificate used by the reference implementation to sign an outgoing (exporting transferblock). The public key material is available via the endpoint: https://localhost:8443/v1/unofficial/.well-known/jwks.json
* springboot-https.p12 - pkcs12 keystore containing the key material for setting up TLS. Since this is a self-signed certificate it requires disabling certificate verification checks.

## Matching public key used for verifying incoming signatures
When receiving an incoming signature the reference implementation needs to retrieve the correct public key to perform the verification with. In the real world these would be public keys from other EBL solution providers.
This reference implementation retrieves the public key by taking the platformHost of one of the transactions and matches this with the CN of the public certificate.
Therefore the CN of the public certificate needs to correspond with the platform. The CN used in the public certificate residing in **dcsa-jwk-verify.jks** is: **localhost:8443**

## Generating a signature locally for testing
When creating a message to be sent to the reference implementation API it is possible to create a valid signature.
Using the tool Smallstep CLI it is possible to create and sign JWS and JWT locally.

Installation instruction for the Smallstep CLI can be found [here](https://smallstep.com/docs/step-cli/installation).

using step CLI the following command can be used to generate and sign a JWS:
```shell
 echo -n \
'{"documentHash":"fd868c82e99777b472a1677390d954dbb0131cb3b0f55c8ef51969856410d38e","previousEnvelopeHash":null,"transactions":[{"action":"ISSU","comments":"The B/L has been issued.","timestamp":1658385166302442200,"isToOrder":true,"platformHost":"localhost:8443","transferee":"43549850248@localhost:8443"}]}' \
 | step crypto jws sign --key verification-private-key.pem
```
it is important to use a "minified" version of the JSON payload and set the platformhost to localhost:8443 so the correct public key is used for verification.

The output of above command is a encoded JWS:
```text
eyJhbGciOiJSUzI1NiIsImtpZCI6IlVhRVdLNmt2ZkRITzNZT2NwUGl2M1RCT2JQTzk2SFZhR2U0czFhUUxBZU0ifQ.eyJkb2N1bWVudEhhc2giOiJmZDg2OGM4MmU5OTc3N2I0NzJhMTY3NzM5MGQ5NTRkYmIwMTMxY2IzYjBmNTVjOGVmNTE5Njk4NTY0MTBkMzhlIiwicHJldmlvdXNFbnZlbG9wZUhhc2giOm51bGwsInRyYW5zYWN0aW9ucyI6W3siaW5zdHJ1Y3Rpb24iOiJJU1NVIiwiY29tbWVudHMiOiJUaGUgQi9MIGhhcyBiZWVuIGlzc3VlZC4iLCJ0aW1lc3RhbXAiOjE2NTgzODUxNjYzMDI0NDIyMDAsImlzVG9PcmRlciI6dHJ1ZSwicGxhdGZvcm1Ib3N0IjoibG9jYWxob3N0Ojg0NDMiLCJ0cmFuc2ZlcmVlIjoiNDM1NDk4NTAyNDhAbG9jYWxob3N0Ojg0NDMifV19.aybAB3RUg_UM2WcvoE4s807Kf7BhSALZq1EvF9f_AUP6ZZgOP4cFS0rlLHSVrobKlF_Og-w0K_M9SPAQS6UnY0hht6pwHTHmoxPmWoQ-ARhnsThjhB3ZYhDbrroJnYPkQAjCmKXPrhNi2z9Fn4GvaI6iRjfJMQchSWQtAEjhqSLNaHtRwec65CDYEZ6OTHX7uP5g3WZtCfmswRGrkcCLosveSxsFpezjjOfTEo2NCCtO0tAxtNtus4GsF8QIMrM6QknJ4909ZrVonvtMUoRffFoUqgemfaFggK5XriLgn2OdSn8ZBhRn_ZqVojuf26mXrVm12C9jSQPPfPGy-myrOQ
```
