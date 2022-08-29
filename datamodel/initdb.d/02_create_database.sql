-- Database setup script
-- Needs to be executed by user postgres or equivalent
\set ON_ERROR_STOP true

-- Cannot be done in a transaction
DROP DATABASE IF EXISTS dcsa_ec_registry;
CREATE DATABASE dcsa_ec_registry OWNER dcsa_db_owner;
\connect dcsa_ec_registry

BEGIN;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; -- Used to generate UUIDs
CREATE SCHEMA IF NOT EXISTS ec_registry;
GRANT ALL PRIVILEGES ON DATABASE dcsa_ec_registry TO dcsa_db_owner;
GRANT ALL PRIVILEGES ON SCHEMA ec_registry TO dcsa_db_owner;
ALTER DEFAULT PRIVILEGES IN SCHEMA ec_registry GRANT ALL ON TABLES TO dcsa_db_owner;
COMMIT;
