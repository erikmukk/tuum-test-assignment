CREATE USER db_owner WITH PASSWORD 'pass';
CREATE DATABASE tuum_db OWNER db_owner ENCODING = 'UTF8' LC_COLLATE = 'et_EE.utf8' LC_CTYPE = 'et_EE.utf8';

\c tuum_db
CREATE USER db_user WITH PASSWORD 'pass';
GRANT CONNECT ON DATABASE tuum_db TO db_user;

--CREATE ROLE rw_access NOLOGIN;

CREATE SCHEMA IF NOT EXISTS tuum AUTHORIZATION db_user;

CREATE TABLE IF NOT EXISTS tuum.account (
    account_id VARCHAR(36) default gen_random_uuid() primary key,
    customer_id VARCHAR(255) NOT NULL,
    country VARCHAR(3) NOT NULL
);

CREATE TABLE IF NOT EXISTS tuum.balance (
    balance_id VARCHAR(36) default gen_random_uuid() primary key,
    account_id VARCHAR(255) NOT NULL,
    amount float8 NOT NULL,
    currency VARCHAR(3) NOT NULL,
    FOREIGN KEY (account_id) REFERENCES tuum.account(account_id)
);

CREATE TABLE IF NOT EXISTS tuum.transaction (
    transaction_id VARCHAR(36) default gen_random_uuid() primary key,
    account_id VARCHAR(255) NOT NULL,
    amount float8 NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description VARCHAR(255) NOT NULL,
    direction VARCHAR(3) NOT NULL,
    FOREIGN KEY (account_id) REFERENCES tuum.account(account_id)
);

GRANT USAGE ON SCHEMA tuum TO db_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA tuum TO db_user;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA tuum TO db_user;