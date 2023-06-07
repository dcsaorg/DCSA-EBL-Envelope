# Test certificates & keys
This directory contains a set of key material to ease a local setup and should not be used for other purposes.

All keys containing in this directory are generated on a local machine and self-signed certificates.

* verification-private-key.pem - the private key of the party sending the eblEnvelope to the test application. This key is used to generate a signature that can be verified by this reference application.
* dcsa-jwk-verify.jks - the JKS containing the public certificate used by the reference implementation to perform a verification of the signature. Contains the public certificate of the keypair of which verification-private-key.pem is the private key.
* dcsa-jwk.jks - contains the private key and public key and certificate used by the reference implementation to sign an outgoing (exporting eblEnvelope). The public key material is available via the endpoint: https://localhost:8443/v1/unofficial/.well-known/jwks.json
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
'{"documentHash":"8dc99d8ac922240c55c03845f49def64187146651bae4f9a63131277cf00d9df","previousEnvelopeHash":null,"transactions":[{"action":"ISSU","comments":"The B/L has been issued.","timestamp":1658385166302442200,"isToOrder":true,"platformHost":"localhost:8443","transferee":"43549850248@localhost:8443"}]}' \
 | step crypto jws sign --key verification-private-key.pem
```
it is important to use a "minified" version of the JSON payload and set the platformhost to localhost:8443 so the correct public key is used for verification.

The output of above command is a encoded JWS:
```text
eyJhbGciOiJSUzI1NiIsImtpZCI6IlVhRVdLNmt2ZkRITzNZT2NwUGl2M1RCT2JQTzk2SFZhR2U0czFhUUxBZU0ifQ.eyJkb2N1bWVudEhhc2giOiI4ZGM5OWQ4YWM5MjIyNDBjNTVjMDM4NDVmNDlkZWY2NDE4NzE0NjY1MWJhZTRmOWE2MzEzMTI3N2NmMDBkOWRmIiwicHJldmlvdXNFbnZlbG9wZUhhc2giOm51bGwsInRyYW5zYWN0aW9ucyI6W3siYWN0aW9uIjoiSVNTVSIsImNvbW1lbnRzIjoiVGhlIEIvTCBoYXMgYmVlbiBpc3N1ZWQuIiwidGltZXN0YW1wIjoxNjU4Mzg1MTY2MzAyNDQyMjAwLCJpc1RvT3JkZXIiOnRydWUsInBsYXRmb3JtSG9zdCI6ImxvY2FsaG9zdDo4NDQzIiwidHJhbnNmZXJlZSI6IjQzNTQ5ODUwMjQ4QGxvY2FsaG9zdDo4NDQzIn1dfQ.c4SJ9-61fE6RmeIuZ3EI-TSM0M6qXuOudtr3YhpDjqVMaYk_RYpaWYvw75ssTbjgGFKTBKCy5lpmOfb8Fq--Qu2k0MWbH6qdX5jTYwl0DX946RQg-hnmVTg9np3bmqVeKqKURyV-UUdG-KK_XCGzPZ-lZkeUlpMcIthQFs0pCODR9GPytv7ZXLPZFOmHM9fn3FD2yRqVhQzcs7HdcxMjCx6hkBW8Z-jW4qteVy2_E9uqjkKwlu_cQLoY83Z0mcjn0PZNQvKF10x7q1_Jjf_Su19UigTUu3pFMrzo4iPS_jcrFoIb3TSZNSzbgAwtujSBFOufPDyEmxlx1sH0ZowMvA
```

the step CLI can also be used during debugging to manually verify signatures of JWS. The below command uses the JWKS in order to verify an EBL envelope JWS:
```shell
echo -n "eyJhbGciOiJSUzI1NiIsImtpZCI6IlVhRVdLNmt2ZkRITzNZT2NwUGl2M1RCT2JQTzk2SFZhR2U0czFhUUxBZU0ifQ.eyJkb2N1bWVudEhhc2giOiI4ZGM5OWQ4YWM5MjIyNDBjNTVjMDM4NDVmNDlkZWY2NDE4NzE0NjY1MWJhZTRmOWE2MzEzMTI3N2NmMDBkOWRmIiwicHJldmlvdXNFbnZlbG9wZUhhc2giOm51bGwsInRyYW5zYWN0aW9ucyI6W3siYWN0aW9uIjoiSVNTVSIsImNvbW1lbnRzIjoiVGhlIEIvTCBoYXMgYmVlbiBpc3N1ZWQuIiwidGltZXN0YW1wIjoxNjU4Mzg1MTY2MzAyNDQyMjAwLCJpc1RvT3JkZXIiOnRydWUsInBsYXRmb3JtSG9zdCI6ImxvY2FsaG9zdDo4NDQzIiwidHJhbnNmZXJlZSI6IjQzNTQ5ODUwMjQ4QGxvY2FsaG9zdDo4NDQzIn1dfQ.c4SJ9-61fE6RmeIuZ3EI-TSM0M6qXuOudtr3YhpDjqVMaYk_RYpaWYvw75ssTbjgGFKTBKCy5lpmOfb8Fq--Qu2k0MWbH6qdX5jTYwl0DX946RQg-hnmVTg9np3bmqVeKqKURyV-UUdG-KK_XCGzPZ-lZkeUlpMcIthQFs0pCODR9GPytv7ZXLPZFOmHM9fn3FD2yRqVhQzcs7HdcxMjCx6hkBW8Z-jW4qteVy2_E9uqjkKwlu_cQLoY83Z0mcjn0PZNQvKF10x7q1_Jjf_Su19UigTUu3pFMrzo4iPS_jcrFoIb3TSZNSzbgAwtujSBFOufPDyEmxlx1sH0ZowMvA" | step crypto jws verify --jwks=test-certificates/jwks-localhost-8443.json --kid=dcsa-kid --alg=PS256
```
Note the step CLI does require the JWKS to be stored in a file.
