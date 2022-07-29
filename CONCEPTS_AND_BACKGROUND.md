# Introduction
## The functions and types of a bill of lading
The Bill of Lading (B/L) is a key document in global trade, which serves three core functions:

- Evidence of the contract of carriage;
- The carrier’s receipt for the goods;
- A document of title to the goods described in the bill.

All three functions are essential in global trade for the sale/purchase and movement of goods, but specifically the third, a document of title, is inherent to the paper B/L as a matter of law. Document of title means that holding the paper B/L amounts to constructive possession of the goods, enabling the holder of the B/L to control and direct the goods, such as by taking delivery or on-selling by way of transfer of possession and title (through endorsement, creating a new titleholder).

Because the B/L is a document of title, it is not only used as a transport document, but also as collateral for financing by banks, an instrument to create trust between parties, and a requirement for insurance. The B/L is issued by the carrier to the shipper at the time the goods are loaded onto a vessel (“shipped onboard”) for transit.

There are two main types of B/L:

- **Straight B/L**: A “non-negotiable” Bill of Lading, with a named consignee, to whom the goods are consigned for delivery. A straight B/L is a document of title to the goods. In principle, goods cannot be released at destination without presenting the original B/L and the named consignee identifying itself.
- **To order B/L**: Often referred to as a “negotiable” bill of lading. In this type of B/L, title is transferable after issuance by the carrier and thus it enables sale of the goods during transit. The final consignee is not named on the B/L, so in principle, the shipment/goods are released to the final lawful titleholder of the B/L (the final endorsee) upon presentation to the carrier.

In addition to the transfer of title, there are parties in the shipping process (like banks, customs and port authorities) that may only take temporary possession/control of the B/L. Both types of B/L described above may be subject to this kind of transfer of possession. An example of a To order B/L process is as follows:

- A shipper submits a booking request to the carrier. The carrier confirms the booking and prepares a B/L. The shipper is named as titleholder.
- When the goods are loaded onto the vessel, the carrier issues the B/L to the shipper. The shipper is now also in possession of the B/L.
- The shipper may endorse the B/L to a subsequent titleholder by putting the subsequent titleholder’s name on it. A chain of signatures is then created (an endorsement chain).
- The new titleholder may now decide if it will surrender the B/L and pick up the goods (making it the consignee), or, in the case of an open endorsement, transfer the eBL to a new titleholder by way of further endorsing the B/L.
- A B/L may, however, only be transferred or surrendered when the receiving party is also in possession of the B/L. For example, when a bank serves as a trustee for the parties, the consignee might be named as titleholder, but possession of the B/L will be transferred to the bank, which will only transfer it to the consignee once it has received payment
- Only once the consignee is named as the titleholder, and is in possession of the B/L, may the consignee surrender the B/L to the carrier. During surrender, the carrier checks if the B/L is valid (proof of origin), if the endorsement chain includes all signatures of previous titleholders, and if the identity of the consignee matches the identity on the B/L. This is the end of the B/L transfer process.

The first version of the technical interoperability standards focusses on use-cases where either the consignee is named (straight eBL) or on to order eBLs where the subsequent possessor is also the subsequent titleholder .
## The challenge of eBL adoption
![BL market share](specifications/market-share.png)

For several decades, ocean carriers and IT companies have been working on the problem of turning the Bill of Lading (B/L) into an electronic Bill of Lading (eBL) to increase efficiency, reduce costs and potentially eliminate the risk of counterfeiting and fraud. Despite all efforts, the adoption rate of the eBL in 2020 was still below 1% (out of all transport documents issued globally in container shipping, based on eBLs issued by DCSA members).

All eBLs that are used are issued and transferred via eBL platforms. One of the big obstacles to eBL adoption is the fragmentation of these platforms, which have their own closed ecosystems, data formats and interfaces for transferring and receiving an eBL. As a result, all stakeholders (carriers, shippers, consignees, and financial institutions) involved in a transaction must be onboarded on the same platform. This is especially challenging for stakeholders that must be able to receive eBLs from all potential shippers and carriers (such as banks, ports and customs agencies), requiring them to adopt many different eBL platforms.

## Standards to increase usage of eBL
DCSA aims to promote interoperability across the eBL platforms used in the global container shipping industry. By allowing all stakeholders to send and receive eBLs in a standardised way, using any eBL platform, the barriers between disparate ecosystems are broken down and network effects may be achieved. This is especially true for stakeholders that must connect to many or all carriers and solution provider platforms (such as financial institutions and large shippers). Standardisation will create a future in which the transfer of an eBL will be as easy as inter-bank payments or sending and receiving e-mails.

![Achieving Interoperability](specifications/interoperability-scope.png)

_**Figure 1:  Achieving interoperability between eBL platforms**_

To be able to exchange an eBL in a way that is functionally and legally equivalent to exchanging a paper-based B/L across platforms, the following standards are required:

1. **Legal standards:** DCSA will publish an Multilateral Solution provider Interoperability Agreement (MSPIA in a separate publication, which will be freely available for adoption. The MSPIA governs the transfer of the eBL envelope between platforms and is not intended to interfere with the legal relationship between users and their Solution Providers (which is governed by their respective bylaws).
2. **Data and interface standards:** A standard format for transferring eBL data has been published by DCSA (Bill of Lading standard 2.0, January 2022) and is freely available at the DCSA website.
3. **Standards for the transfer of eBL title and possession (this document):** Standards to enable transfer of title and possession of eBLs across eBL platforms, while ensuring:
   - Only one legal entity may be in control of the eBL at any time.
   - Immutability when eBL title and possession is transferred across eBL platforms.
   - Non-Repudiation upon issuance, transfer, and release of the eBL by providing proof of the sender’s identity (to recipient) and proof of acceptance (to sender).

## The scope of this publication
This publication provides standards and endorses existing standards for technical interoperability, existing of the following 3 eBL building blocks:

- **eBL Envelope:** A standard digital data container that is transferred between eBL platforms, which includes information about the eBL titleholder & possessor and endorsement chain.
- **Digital addresses:** Public/private key pairs used to sign and transfer eBL envelopes irrevocably between eBL platforms.
- **Digital identities:** Standardised digital identities ensure that the Legal Identity of a titleholder & possessor is recognised across eBL platforms. Digital identities may also support banks and carriers in compliance regimes by simplifying the verification of the corresponding entity.

![Exchange eBL between platforms](specifications/cross-platform-exchange.png)

_**Figure 2: Simple example of an eBL envelope that Is transferred between platforms**_

In the next section of this document the 3 building blocks to transfer eBL title and possession across eBL platforms will be defined. In chapter 3 the process supported by these standards will be detailed.

# Standards for the secure transfer of eBL possession and title
## eBL envelope
## eBL envelope structure
A digital envelope is a secure electronic data container. The eBL envelope is used to capture information about the eBL titleholder & possessor and endorsement chain, which is transferred between eBL platforms. For the envelope structure, the following standard is used:

| Envelope structure       | Explanation                                                                                                                                                                                                                                                                                         |
|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| JSON Web Signature (JWS) | A JSON Web Signature (JWS) is an open standard ([RFC 7515](https://datatracker.ietf.org/doc/html/rfc7515))) that defines a compact and self-contained way for securely transmitting information between parties as a JSON object. This information may be verified and trusted because it is digitally signed. |

_**Table 1: eBL Envelope Structure**_

In the future, the eBL envelope content might also be transferred as a Non-Fungible Token (NFT), which is a cryptographic asset on a blockchain. The standardised eBL envelope content may therefore also be used within an NFT.

## eBL envelope content
The information transferred in the eBL envelope structure between eBL platforms is the eBL envelope content, generated by the eBL platforms based on transfers initiated by their users (carriers, shippers, banks, consignees). The content in the eBL envelope consists of several types of information:

**General eBL Information**

The eBL envelope includes the eBL data fields (as published in the DCSA Bill of Lading standard 2.0, January 2022). Upon issuance, also a unique fingerprint (a hash) is created for the eBL data and included in the eBL envelope, creating a verifiable unique link to the eBL. An eBL may always be verified by creating a unique hash of the eBL data and matching that hash to the hash in the eBL envelope, thereby ensuring that it is the same bill of lading being transferred between the platforms.



|**Data field(s)**| **Format**                                       |
|-----------------|--------------------------------------------------|
|**eBL data fields**| *See DCSA Bill of Lading standard 2.0*           |
|**eBL document Identifier**| Unique Hash (#) of the eBL document              |
|**Is To Order**| Yes/No (flagged if yes)                          |

_**Table 2: eBL identifier information**_

**Possessor & titleholder identity information**

The possessor & titleholder is indicated with a unique identifier. The primary identifier is the local  identifier that is used on an eBL platform, in combination with the Name of the eBL platform. Additionally, extra global identifiers can be added by the sender, like LEI and DID, requesting the receiving platform to perform additional verifications of their user/the receiver of the eBL. More information about public/private keys and identifiers is included in sections 2.2 and 2.3.

|**Data field**| **Format**                                          |
|---------------|-----------------------------------------------------|
|**Identifier possessor (primary)**| \<Local identifier>@<Platformname.com>              |
|**Identifier possessor (additional)**| Format of Identifier  (I.e., LEI, DID, Public Keys) |
|||
|*When title and possession are not the same, also the below fields need to be added:*|
|**Identifier title holder (primary)**| <Local identifier@Platformname>.com>                |
|**Identifier title holder(additional)**| Format of Identifier  (I.e., LEI, DID, Public Keys) |

_**Table 3: Possessor & titleholder identity information**_

**Endorsement chain information**

When possession & title of an eBL are transferred, a chain of transactions is created (a possession and/or endorsement chain). When an eBL is transferred across eBL platforms, also the overview of all previous transfers is transferred to the next platform. The sending eBL platform guarantees the correctness of the information by signing the eBL envelope with a public/private key, legally governed by the MSPIA. Proof that the user initiated a transfer is available on the eBL platform where the transfer happened (for example in the form of the user’s digital signature).

In table 4, a standard format for the possession and/or endorsement chain is included. The information about each transfer in the possession and/or endorsement chain is created and provided by the eBL platform that initiated each transfer.

|**State changes (#)**|**eBL identifier**|**Time stamp <br>(incl. time zone)**|**Identifier Possessor (primary)**|<p>**Identifier (s)**</p><p>**possessor (additional)**</p>|**eBL platform possessor**|<p>**Identifier (s)**</p><p>**Title holder <br>(if ≠ possessor)**</p>|<p>**eBL platform Title Hold**</p><p>**(if** **≠ possessor)**</p>|**User signature (optional)**|**Platform signature**| **Status**                         |
|---------------------|-----------------------|-------------------------------|----------------------------------|----------------------------------------------------------|---------------------------|--------------------------------------------------------------------|-----------------------------------------------------------------|-----------------------------|----------------------|------------------------------------|
|7|#123|16:24 11-12 CET|#1E34  (Carrier)|LEI code|SP A|#5L81 (Consignee)|SP E|Sign.|PK (SP E)| Surrender                          |
|6|#123|12:44 9-12 CET|#5L81 (Consignee)|LEI code|SP E|#5L81 (Consignee)|SP E|Sign.|PK (SP D)| Possession transferred             |
|5|#123|12:44 9-12 CET|#3E99 (Bank 2)|LEI code|SP D|#5L81 (Consignee)|SP E|Sign.|PK (SP C)| Possession transferred             |
|4|#123|12:25 8-12 CET|#8R54 (Bank 1)|LEI code|SP C|#5L81 (Consignee)|SP E|Sign.|PK (SP B)| Possession transferred             |
|3|#123|09:18 8-12 CET|#6R61 (Shipper)|LEI code|SP B|#5L81 (consignee)|SP E|Sign.|PK (SP B)| Title transferred                  |
|2|#123|10:23 7-12 CET|#6R61 (Shipper)|LEI code|SP B|#6R61 (Shipper)|SP B|Sign.|PK (SP A)| Issuance                           |
|1|#123|07:23 7-12 CET|#1E34 (Carrier)|LEI code|SP A|X|X|Sign.|X| eBL creation                       |

_**Table 4: Simple example data structure possession & endorsement chain for a to order eBL**_

**Digital signature**

When the eBL envelope is transferred,  the eBL envelope is signed with a private key of the sending platform to the public key of  the receiving platform. A digital signature with a public/private key pair makes the signed data immutable and ensures irrevocability of a transfer.

|**Data field**| **Format**                         |
|--------------|------------------------------------|
|**Signature of sending eBL platform**| Private key signature platform     |

_**Table 5: Digital signature**_

## Digital address of the eBL platform
As mentioned above, to transfer an eBL from one platform to the next platform, public/private key pairs are used. A key pair is a combination of a public key (a known address, like the address of a house in the physical world), and a private key (a proof that someone is in control of a public key, like the key to a house in the physical world).

In the transfer process, the sending platform is responsible for correct creation of the eBL envelope. By digitally signing the eBL envelope with the private key of the sender to the public key of the receiver, irrevocable proof is created that the eBL envelope data was signed in this state by the sending eBL platform to the receiving eBL platform. The same methods are applied within eBL platforms to transfer an eBL from one user to another (but this differs per eBL platform).

[//]: # (ToDo change appendix reference to link to technical spec)
Public keys are linked to a known legal entity, thus ensuring verification of the identity of the legal entity. This link may be created by a trusted entity (such as a certificate authority, government organisation or trusted party) that has verified the identity of the party holding the public key.
Requirements for linking a public/private key pair to a legal entity via a digital certificate are found section  **Additional specifications and requirements around security** in [The general Readme](README.md) .

```text
Public keys may also be self governed. In the PoC of the Interoperability standards,  eBL Platforms generate their own public keys for the platform.
These public keys can for example be made known across platforms by uploading them to an external digital repository (upload only no deletion).
This digital repository will be used by other eBL platforms to validate public keys.
```

## Digital identities
To transfer an eBL to another platform, the entity/platform user that receives the eBL must be identified with a unique digital identifier. A digital identifier creates a unique link between the identity of a legal entity and its representation in the digital world. For digital identities, 3 aspects are essential:

- **Identification:** The act of indicating an identity. For example: a string of letters and/or text that is uniquely linked to a legal entity (e.g. company or person).
- **Authentication:** The act of proving an identity. For example, a passport, username and password or digital certificate that may be used to verify the identity of a person or company.
- **Authorisation:** The function of specifying access rights/privileges that are linked to an identifier. For example, only certain identities within an organisation might have permission to transfer an eBL.

The primary identifier used to indicate the receiver on another platform, will a local identifier used on a platform in combination with the platform name.  This number is truly unique to a specific account, and is often obtained after a user has gone trough the onboarding and Know Your Customer (KYC) processes of the eBL platform or carrier (see appendix A for more detailed requirements for Identification of legal entities).

The disadvantage of local identifiers is that they are often specific to an organization and/or country and are often hard to lookup/validate. The sending user has therefore also the option to add an additional global identifier to the transfer of an eBL, to indicate the next possessor and title holder. This (optional) additional identifier must be validated by the receiving platform and be matched to information of their user to accept the eBL.

Global identifier must meet the following requirements:

- The identifier is globally accessible
- The identifier is globally verifiable (for example in a public address book)
- The identifier is based on international standards

The following global identifiers are identified so far that meet these requirements:

|**Global identifier**|**Issuer**| **Link**                                                                                  |
|---------------------|----------|-------------------------------------------------------------------------------------------|
|**LEI (Legal Entity Identifier)**|<p><https://www.gleif.org/>. The Legal Entity Identifier (LEI) is a 20-digit, alphanumeric code that connects to key reference information that enables clear and unique identification of companies participating in global financial markets. The LEI is based on the ISO 17442 standard.</p><p>GLEIF[^1] certification authority</p>| <http://search.gleif.org>                                                                 |
|**DID – Verifiable credentials**|Decentralised| <p><https://www.w3.org/TR/did-core/> </p><p><https://www.w3.org/TR/vc-data-model/></p>    |
|***More might be added***|

_**Table 6: Global identifiers**_

# Process description(s) for transfer of eBL possession & title
## Introduction to the process
This section sets out the process of transferring the eBL possession (=control) and title in an eBL envelope between eBL platforms. The process has several prerequisites to work:

- The carrier, shipper, consignee and/or bank have joined an eBL platform and have agreed to the bylaws of the platform they have joined.
- eBL platform solution providers have signed the MSPIA, governing the transfer of the eBL envelope between platforms.
- Each platform has obtained a public/private key pair that is used to sign the eBL envelope.

There are different variations of the transfer process depending on the type of eBL and the scope of involvement by banks.

|**Type of eBL**|**Banks not involved**| **Banks involved**                                                                                                                                                                                                                                                                                                                                                                            |
|---------------|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|**Straight eBL**|<p>- The consignee is named upon issuance of the eBL and cannot be changed afterwards</p><p>- The shipper transfers eBL possession directly to the consignee </p>| <p>- The consignee is named upon issuance of the eBL and cannot be changed afterwards</p><p>- The shipper transfers possession to the bank, which in turn will transfer possession to the consignee (typically upon payment)</p>                                                                                                                                                              |
|**To-Order eBL**|- The eBL is issued to the Shipper. The eBL may be endorsed to a next titleholder, when the titleholder is also in possession of the eBL, and so on (as long as it is an open endorsement), until it is surrendered to the Carrier.| <p>- The eBL is issued to the Shipper. The eBL may be endorsed to a next titleholder, when the titleholder is also in possession of the eBL, and so on (as long as it is an open endorsement), until it is surrendered to the Carrier.</p><p>- Possession (and often title) is also transferred to the bank, which will transfer it to the consignee (typically upon payment).</p>            |

_**Table 7: variations of the transfer process**_

The first version of the technical interoperability standards focusses on use-cases where either the consignee is named (straight eBL) or on to order eBLs where the subsequent possessor is also the subsequent titleholder.

Furthermore, the focus of these standards is on the transfer between eBL platforms. Title and possession may, however, also be transferred within a platform. An eBL envelope is only composed and transferred when eBL possession is transferred to a party on another platform.

## Transfer process
A visualisation of the transfer process of interoperable eBL is shown in figure 3.

![Transfer process](specifications/bl-process.png)

_**Figure 3: Transfer process of the eBL envelope with banks involved**_

**Step 1A: The carrier Issues the eBL to the shipper on another eBL platform**

1. The carrier prepares the eBL based on the shipping instruction
1. The carrier indicates the receiver of the eBL envelope
   (The Name , Identifier and eBL platform of the next  possessor & title holder. This information was included in the SI)
1. The carrier platform populates and digitally signs the eBL envelope
1. The carrier platform authenticates with the receiving platform (using OIDC over mTLS) and performs the PUT request to the API of the shipper platform

**Step 1B: The shipper (platform) receives the eBL**

1. The Shipper platform receives the PUT request and receives the eBL envelope
1. When the eBL envelope is received successfully, a signed accept message is sent to the carrier platform. When the eBL envelope is rejected, a signed reject message is sent to the carrier platform.
1. After it received a signed accept message from the shipper platform, the eBL is marked transferred on the carrier platform.
1. The shipper platform processes the eBL (envelope) data and associates the eBL to the appropriate shipper.

**Step 2: The shipper transfers possession to the advising bank**

**Step 3: The advising bank transfer possession to the issuing bank**

**Step 4: The issuing bank transfer possession to the consignee**

**Step 5: The consignee surrenders (transfer possession) the eBL to the carrier**

_\*For this transfer the steps of 1-2 apply, with the advising bank/issuing bank/consignee/carrier as the next possessor & title holder._

```text
Differences between the to order and straight eBL transfer process.
The process of transferring a to order eBL is similar to the process of transferring a straight eBL, the difference being:
- With a straight eBL the consignee is already named.
- The to order eBL is a negotiable instrument, where the consignee can be changed by endorsement. Thereby taking into account that:
   - The consignee/title holder can only be changed when the party that is doing the endorsement is also in possession of the eBL.
   - An endorsement is completed once the endorsee has received the bl and confirmed the endorsement by I.e. signature.
   - Every title transfer/endorsement is recorded in the endorsement chain by the eBL platform of the current possessor of the eBL.
```

## Exception processes
In this section, exception processes are defined for transferring the eBL envelope.

**Required amendments to the electronic Bill of Lading**

- The eBL envelope is surrendered to the carrier (by transferring possession to the eBL platform of the carrier, as described in chapter 3.2).
- The carrier invalidates the eBL. The carrier issues a new eBL.

**Switch to paper**

- If, for some reason, the bank(s) or consignee (or other involved party) require a physical (paper) original B/L, the eBL will be surrendered to the carrier.
- The carrier will then issue a new physical (paper) B/L, including proof of past endorsement(s) until the point of the switch to paper.

## Potential risks
**How to reissue eBL to the correct party in case a wrong identifier was selected**

- When a non-existing identifier is filled or selected, the eBL should be reissued.
- When the eBL is sent to the wrong party, the receiver should be contacted to return the eBL to the original sender.
- In case both title and possession are sent to the wrong party - and the party does not respond - the sending party might consider taking legal steps.

**Who is liable in case of error?**

- The general notion of the MSPIA is that each sending user is liable for the accuracy of the data it transfers via their platform.
- Whether it is the fault of the user or their platform is a question to be considered on a case-by-case basis, as well as the specific user/solution provider bylaws.

**Is there a risk that a party could acquire liabilities under an eBL before it receives an updated eBL envelope?**

- Once a B/L is surrendered to the carrier, rights under the eBL would be transferred back as well.

**What action can a holder take if a platform is responsible for the incorrect information and the sender is unwilling to act?**

- In a framework of interoperability, a user should have the same rights as if he was receiving an eBL from a user using the same platform as himself. The Solution Provider bylaws should cover situations such as these. The MSPIA governs the transfer of the eBL envelope between platforms and is not intended to interfere with the legal relationship between users and their solution providers.

**How can splitting/forking the document be avoided? (resulting in more versions of an eBL)**

- Exposing the eBL envelope to more than one party should be technically impossible. Preventing this is a key task of eBL platforms. The interoperability standards include security mechanisms such as public/private key authentication and confirmation messages when the data is successfully received. Liabilities for when things do go wrong will be clearly covered in the MSPIA.

# Conclusion and call to action
This document provides standards (beta) to securely transfer eBL title and possession across eBL platforms. Standards for interoperability should benefit all stakeholders in the E2E documentation process and facilitate eBL adoption by allowing industry stakeholders to use their preferred eBL platform. This will support the container shipping industry in ultimately growing to a 100% adoption rate.

DCSA invites you to share feedback on the standards introduced in this document. In 2022 DCSA will plan multiple PoCs and pilots to test and refine its eBL standards. Please reach out to DCSA if you are interested in joining one or more of these initiatives.


# Appendix

## Appendix A: Requirements for Identification of Legal entities
For the identification of a Legal Entity, a trusted entity (like a government organisation or certificate authority), must acquire the information in the table below from the subject (or receive this information from the carrier if relying on the carrier’s onboarding process).

|**Identity information required at registration**| **Source / contents**                               |
|-------------------------------------------------|-----------------------------------------------------|
|Global identifier type| Global identifiers as defined in chapter 2.4.       |
|Global identifier| Global identifier registry (see above)              |
|**Commercial registry number**| Government registry                                 |
|Legal Trading Name| Government registry                                 |
|Country of incorporation| Government registry                                 |
|Tax ID of legal entity - including country identifier)| Tax authority                                       |
|**Company website (Optional)**| Provided by Employee                                |
|**Company Email address**| Provided by Employee                                |
|Full Address of legal entity| Government registry                                 |
|Full name of employee requesting certificate| Government issued ID document                       |
|Employee e-mail address (Optional)| Provided by employee                                |
|Employee phone number (Optional)| Provided by employee                                |
|Role of employee in legal entity organisation **and proof of authorisation to request the certificate.**| Provided by employee (with evidence)                |
