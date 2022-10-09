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

CREATE EXTENSION IF NOT EXISTS hstore;

CREATE SCHEMA audit;
REVOKE ALL ON SCHEMA audit FROM public;

CREATE TABLE audit.logged_actions (
                                      event_id bigserial primary key,
                                      schema_name text not null,
                                      table_name text not null,
                                      action_tstamp_tx TIMESTAMP WITH TIME ZONE NOT NULL,
                                      action_tstamp_stm TIMESTAMP WITH TIME ZONE NOT NULL,
                                      action_tstamp_clk TIMESTAMP WITH TIME ZONE NOT NULL,
                                      transaction_id bigint,
                                      client_query text,
                                      action TEXT NOT NULL CHECK (action IN ('I','D','U', 'T')),
                                      row_data hstore,
                                      changed_fields hstore,
                                      statement_only boolean not null
);

REVOKE ALL ON audit.logged_actions FROM public;

CREATE OR REPLACE FUNCTION audit.if_modified_func() RETURNS TRIGGER AS $body$
DECLARE
    audit_row audit.logged_actions;
    include_values boolean;
    log_diffs boolean;
    h_old hstore;
    h_new hstore;
    excluded_cols text[] = ARRAY[]::text[];
BEGIN
    IF TG_WHEN <> 'AFTER' THEN
        RAISE EXCEPTION 'audit.if_modified_func() may only run as an AFTER trigger';
    END IF;

    audit_row = ROW(
        nextval('audit.logged_actions_event_id_seq'), -- event_id
        TG_TABLE_SCHEMA::text,                        -- schema_name
        TG_TABLE_NAME::text,                          -- table_name
                current_timestamp,                            -- action_tstamp_tx
        statement_timestamp(),                        -- action_tstamp_stm
        clock_timestamp(),                            -- action_tstamp_clk
        txid_current(),                               -- transaction ID
        current_query(),                              -- top-level query or queries (if multistatement) from client
        substring(TG_OP,1,1),                         -- action
        NULL, NULL,                                   -- row_data, changed_fields
        'f'                                           -- statement_only
        );

    IF NOT TG_ARGV[0]::boolean IS DISTINCT FROM 'f'::boolean THEN
        audit_row.client_query = NULL;
    END IF;

    IF TG_ARGV[1] IS NOT NULL THEN
        excluded_cols = TG_ARGV[1]::text[];
    END IF;

    IF (TG_OP = 'UPDATE' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = hstore(OLD.*) - excluded_cols;
        audit_row.changed_fields =  (hstore(NEW.*) - audit_row.row_data) - excluded_cols;
        IF audit_row.changed_fields = hstore('') THEN
            -- All changed fields are ignored. Skip this update.
            RETURN NULL;
        END IF;
    ELSIF (TG_OP = 'DELETE' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = hstore(OLD.*) - excluded_cols;
    ELSIF (TG_OP = 'INSERT' AND TG_LEVEL = 'ROW') THEN
        audit_row.row_data = hstore(NEW.*) - excluded_cols;
    ELSIF (TG_LEVEL = 'STATEMENT' AND TG_OP IN ('INSERT','UPDATE','DELETE','TRUNCATE')) THEN
        audit_row.statement_only = 't';
    ELSE
        RAISE EXCEPTION '[audit.if_modified_func] - Trigger func added as trigger for unhandled case: %, %',TG_OP, TG_LEVEL;
        RETURN NULL;
    END IF;
    INSERT INTO audit.logged_actions VALUES (audit_row.*);
    RETURN NULL;
END;
$body$
    LANGUAGE plpgsql
    SECURITY DEFINER
    SET search_path = pg_catalog, public;

CREATE OR REPLACE FUNCTION audit.audit_table(target_table regclass, audit_rows boolean, audit_query_text boolean, ignored_cols text[]) RETURNS void AS $body$
DECLARE
    stm_targets text = 'INSERT OR UPDATE OR DELETE OR TRUNCATE';
    _q_txt text;
    _ignored_cols_snip text = '';
BEGIN
    EXECUTE 'DROP TRIGGER IF EXISTS audit_trigger_row ON ' || target_table;
    EXECUTE 'DROP TRIGGER IF EXISTS audit_trigger_stm ON ' || target_table;

    IF audit_rows THEN
        IF array_length(ignored_cols,1) > 0 THEN
            _ignored_cols_snip = ', ' || quote_literal(ignored_cols);
        END IF;
        _q_txt = 'CREATE TRIGGER audit_trigger_row AFTER INSERT OR UPDATE OR DELETE ON ' ||
                 target_table ||
                 ' FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func(' ||
                 quote_literal(audit_query_text) || _ignored_cols_snip || ');';
        RAISE NOTICE '%',_q_txt;
        EXECUTE _q_txt;
        stm_targets = 'TRUNCATE';
    ELSE
    END IF;

    _q_txt = 'CREATE TRIGGER audit_trigger_stm AFTER ' || stm_targets || ' ON ' ||
             target_table ||
             ' FOR EACH STATEMENT EXECUTE PROCEDURE audit.if_modified_func('||
             quote_literal(audit_query_text) || ');';
    RAISE NOTICE '%',_q_txt;
    EXECUTE _q_txt;

END;
$body$
    language 'plpgsql';

CREATE OR REPLACE FUNCTION audit.audit_table(target_table regclass, audit_rows boolean, audit_query_text boolean) RETURNS void AS $body$
SELECT audit.audit_table($1, $2, $3, ARRAY[]::text[]);
$body$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION audit.audit_table(target_table regclass) RETURNS void AS $body$
SELECT audit.audit_table($1, BOOLEAN 't', BOOLEAN 't');
$body$ LANGUAGE 'sql';

GRANT USAGE ON SCHEMA audit to db_user;
GRANT SELECT ON ALL TABLES IN SCHEMA audit to db_user;

SELECT audit.audit_table('tuum.account');
SELECT audit.audit_table('tuum.balance');
SELECT audit.audit_table('tuum.transaction');

GRANT USAGE ON SCHEMA tuum TO db_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA tuum TO db_user;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA tuum TO db_user;