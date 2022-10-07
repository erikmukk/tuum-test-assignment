CREATE USER db_owner WITH PASSWORD 'it';

--CREATE ROLE rw_access NOLOGIN;

CREATE SCHEMA IF NOT EXISTS tuum AUTHORIZATION db_owner;

CREATE TABLE IF NOT EXISTS tuum.account (
                                            account_id VARCHAR(36) default gen_random_uuid() primary key,
                                            customer_id VARCHAR(255) NOT NULL
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
                                                description TEXT NOT NULL,
                                                direction VARCHAR(3) NOT NULL,
                                                FOREIGN KEY (account_id) REFERENCES tuum.account(account_id)
);
