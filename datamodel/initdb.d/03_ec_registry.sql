\set ON_ERROR_STOP true
\connect dcsa_ec_registry

BEGIN;

DROP TABLE IF EXISTS ec_registry.transportdocument CASCADE;
CREATE TABLE ec_registry.transportdocument (
	document_hash varchar(64) NOT NULL PRIMARY KEY,
	transport_document_json text NOT NULL,
	is_exported bool NOT NULL DEFAULT false
);

DROP TABLE IF EXISTS ec_registry.ebl_envelope CASCADE;
CREATE TABLE ec_registry.ebl_envelope (
  envelope_hash varchar(64) NOT NULL PRIMARY KEY,
  previous_envelope_hash varchar(64) NULL UNIQUE,
  signature text NOT NULL,
  document_hash varchar(64) NOT NULL
);
ALTER TABLE ec_registry.ebl_envelope ADD FOREIGN KEY (document_hash) REFERENCES ec_registry.transportdocument(document_hash);

DROP TABLE IF EXISTS ec_registry."party" CASCADE;
CREATE TABLE ec_registry."party" (
  ebl_platform_identifier varchar(255) NOT NULL PRIMARY KEY,
  party_name varchar(255) NOT NULL,
  registration_number varchar(255) NOT NULL,
  country_of_registration varchar(2) NOT NULL,
  address text,
  tax_reference varchar(255),
  lei varchar(255),
  did varchar(255)
);

DROP TABLE IF EXISTS ec_registry."transaction" CASCADE;
CREATE TABLE ec_registry."transaction" (
  id uuid NOT NULL PRIMARY KEY,
  "comments" varchar(255) NULL,
  document_hash varchar(64) NOT NULL,
  "action" varchar(4) NOT NULL,
  is_to_order bool NULL,
  platform_host varchar(255) NULL,
  "timestamp" int8 NOT NULL,
  transferee varchar(255) NULL,
  envelope_hash varchar(64) NULL,
  CONSTRAINT uniquetimestampanddocumenthash UNIQUE ("timestamp", document_hash)
);

ALTER TABLE ec_registry."transaction" ADD FOREIGN KEY (document_hash) REFERENCES ec_registry.transportdocument (document_hash);
ALTER TABLE ec_registry."transaction" ADD FOREIGN KEY (envelope_hash) REFERENCES ec_registry.ebl_envelope (envelope_hash);
ALTER TABLE ec_registry."transaction" ADD FOREIGN KEY (transferee) REFERENCES ec_registry.party (ebl_platform_identifier);

COMMIT;
