-- DROP DATABASE piazza;

-- CREATE DATABASE piazza
--     WITH
--     OWNER = postgres
--     ENCODING = 'UTF8'
--     LC_COLLATE = 'en_US.UTF-8'
--     LC_CTYPE = 'en_US.UTF-8'
--     TABLESPACE = pg_default
--     CONNECTION LIMIT = -1;

CREATE TABLE IF NOT EXISTS users(
userId SERIAL PRIMARY KEY,
userName CHAR(20) UNIQUE NOT NULL,
firstName CHAR(20)  NOT NULL,
lastName CHAR(20)  NOT NULL,
email CHAR(100) UNIQUE NOT NULL,
password TEXT NOT NULL,
role CHAR(20),
createdAt TIMESTAMPTZ NOT NULL DEFAULT NOW(),
updatedAt TIMESTAMPTZ NOT NULL DEFAULT NOW());

CREATE INDEX IF NOT EXISTS userNameIndex ON users(userName);
CREATE INDEX IF NOT EXISTS emailIndex ON users(email);

CREATE OR REPLACE FUNCTION trigger_set_timestamp() RETURNS TRIGGER AS $$ BEGIN NEW.updatedAt = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_timestamp ON users;
CREATE TRIGGER set_timestamp BEFORE INSERT OR UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP FUNCTION IF EXISTS register_user;
CREATE OR REPLACE FUNCTION register_user( inputUserName CHAR(20), inputFirstName CHAR(20), inputLastName CHAR(20), inputEmail CHAR(100), inputPassword TEXT, inputRole CHAR(20)) RETURNS REFCURSOR AS $$ DECLARE cursor REFCURSOR := 'cur'; BEGIN INSERT INTO users(userName, firstName, lastName, email, password, role) VALUES($1, $2, $3, $4, crypt($5, gen_salt('bf')), $6); OPEN cursor FOR SELECT users.userId FROM users WHERE users.userName = $1; RETURN cursor; END; $$ LANGUAGE PLPGSQL;

DROP FUNCTION IF EXISTS login_user;
CREATE OR REPLACE FUNCTION login_user( inputEmail CHAR(100), inputPassword CHAR(20)) RETURNS REFCURSOR AS $$ DECLARE cursor REFCURSOR := 'cur'; BEGIN OPEN cursor FOR SELECT users.userName, users.role FROM users WHERE email = $1 AND password = crypt($2, password); RETURN cursor; END; $$ LANGUAGE PLPGSQL;

DROP FUNCTION IF EXISTS delete_user;
CREATE OR REPLACE FUNCTION delete_user(inputUserName CHAR(20)) RETURNS VOID AS $$ BEGIN DELETE FROM users WHERE users.userName = $1; END; $$ LANGUAGE PLPGSQL;

DROP FUNCTION IF EXISTS update_user;
CREATE OR REPLACE FUNCTION update_user( inputFirstName VARCHAR(20), inputLastName VARCHAR(20), inputPassword TEXT, inputUserName CHAR(20)) RETURNS VOID AS $$ BEGIN UPDATE users SET firstName = COALESCE($1, firstName), lastName = COALESCE($2, lastName), password = COALESCE(crypt($3, gen_salt('bf')), password) WHERE userName = $4; END; $$ LANGUAGE PLPGSQL;