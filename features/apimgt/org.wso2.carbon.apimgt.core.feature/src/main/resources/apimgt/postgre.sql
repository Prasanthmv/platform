BEGIN TRANSACTION;
	
CREATE TABLE IDN_BASE_TABLE (
            PRODUCT_NAME VARCHAR (20),
            PRIMARY KEY (PRODUCT_NAME)
)
;

INSERT INTO IDN_BASE_TABLE values ('WSO2 Identity Server');

CREATE TABLE IDN_OAUTH_CONSUMER_APPS (
            CONSUMER_KEY VARCHAR (512),
            CONSUMER_SECRET VARCHAR (512),
            USERNAME VARCHAR (255),
            TENANT_ID INTEGER DEFAULT 0,
            APP_NAME VARCHAR (255),
            OAUTH_VERSION VARCHAR (128),
            CALLBACK_URL VARCHAR (1024),
            PRIMARY KEY (CONSUMER_KEY)
)
;

CREATE TABLE IDN_OAUTH1A_REQUEST_TOKEN (
            REQUEST_TOKEN VARCHAR (512),
            REQUEST_TOKEN_SECRET VARCHAR (512),
            CONSUMER_KEY VARCHAR (512),
            CALLBACK_URL VARCHAR (1024),
            SCOPE VARCHAR(2048),
            AUTHORIZED VARCHAR (128),
            OAUTH_VERIFIER VARCHAR (512),
            AUTHZ_USER VARCHAR (512),
            PRIMARY KEY (REQUEST_TOKEN),
            FOREIGN KEY (CONSUMER_KEY) REFERENCES IDN_OAUTH_CONSUMER_APPS(CONSUMER_KEY)
)
;


CREATE TABLE IDN_OAUTH1A_ACCESS_TOKEN (
            ACCESS_TOKEN VARCHAR (512),
            ACCESS_TOKEN_SECRET VARCHAR (512),
            CONSUMER_KEY VARCHAR (512),
            SCOPE VARCHAR(2048),
            AUTHZ_USER VARCHAR (512),
            PRIMARY KEY (ACCESS_TOKEN),
            FOREIGN KEY (CONSUMER_KEY) REFERENCES IDN_OAUTH_CONSUMER_APPS(CONSUMER_KEY)
)
;

CREATE TABLE IDN_OAUTH2_AUTHORIZATION_CODE (
            AUTHORIZATION_CODE VARCHAR (512),
            CONSUMER_KEY VARCHAR (512),
            SCOPE VARCHAR(2048),
            AUTHZ_USER VARCHAR (512),
            TIME_CREATED TIMESTAMP,
            VALIDITY_PERIOD BIGINT,
            PRIMARY KEY (AUTHORIZATION_CODE),
            FOREIGN KEY (CONSUMER_KEY) REFERENCES IDN_OAUTH_CONSUMER_APPS(CONSUMER_KEY)
)
;

CREATE TABLE IDN_OAUTH2_ACCESS_TOKEN (
			ACCESS_TOKEN VARCHAR (255),
			REFRESH_TOKEN VARCHAR (255),
			CONSUMER_KEY VARCHAR (255),
			AUTHZ_USER VARCHAR (255),
			USER_TYPE VARCHAR (255),
			TIME_CREATED TIMESTAMP,
			VALIDITY_PERIOD BIGINT,
			TOKEN_SCOPE VARCHAR (512),
			TOKEN_STATE VARCHAR (128) DEFAULT 'ACTIVE',
			PRIMARY KEY (ACCESS_TOKEN),
            FOREIGN KEY (CONSUMER_KEY) REFERENCES IDN_OAUTH_CONSUMER_APPS(CONSUMER_KEY)
)
;


CREATE SEQUENCE AM_SUBSCRIBER_SEQUENCE START WITH 1 INCREMENT BY 1;
CREATE TABLE AM_SUBSCRIBER (
    SUBSCRIBER_ID INTEGER DEFAULT nextval('am_subscriber_sequence'),
    USER_ID VARCHAR(50) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    EMAIL_ADDRESS VARCHAR(256) NULL,
    DATE_SUBSCRIBED DATE NOT NULL,
    PRIMARY KEY (SUBSCRIBER_ID),
    UNIQUE (TENANT_ID,USER_ID)
)
;

--TODO: Have to add ON UPDATE CASCADE for the FOREIGN KEY(SUBSCRIBER_ID) relation
CREATE SEQUENCE AM_APPLICATION_SEQUENCE START WITH 1 INCREMENT BY 1 ;
CREATE TABLE AM_APPLICATION (
    APPLICATION_ID INTEGER DEFAULT nextval('am_application_sequence'),
    NAME VARCHAR(100),
    SUBSCRIBER_ID INTEGER,
    APPLICATION_TIER VARCHAR(50) DEFAULT 'Unlimited',
    FOREIGN KEY(SUBSCRIBER_ID) REFERENCES AM_SUBSCRIBER(SUBSCRIBER_ID) ON DELETE CASCADE,
    PRIMARY KEY(APPLICATION_ID),
    UNIQUE (NAME,SUBSCRIBER_ID)
)
;
CREATE SEQUENCE AM_API_SEQUENCE START WITH 1 INCREMENT BY 1;

CREATE TABLE AM_API (
    API_ID INTEGER DEFAULT nextval('am_api_sequence'),
    API_PROVIDER VARCHAR(256),
    API_NAME VARCHAR(256),
    API_VERSION VARCHAR(30),
    CONTEXT VARCHAR(256),
    PRIMARY KEY(API_ID),
    UNIQUE (API_PROVIDER,API_NAME,API_VERSION)
)
;
;
--TODO: Have to add ON UPDATE CASCADE for the FOREIGN KEY(SUBSCRIPTION_ID) relation
CREATE SEQUENCE AM_SUBSCRIPTION_SEQUENCE START WITH 1 INCREMENT BY 1;
CREATE TABLE AM_SUBSCRIPTION (
    SUBSCRIPTION_ID INTEGER DEFAULT nextval('am_subscription_sequence'),
    TIER_ID VARCHAR(50),
    API_ID INTEGER,
    LAST_ACCESSED DATE NULL,
    APPLICATION_ID INTEGER,
    FOREIGN KEY(APPLICATION_ID) REFERENCES AM_APPLICATION(APPLICATION_ID) ON DELETE CASCADE,
    FOREIGN KEY(API_ID) REFERENCES AM_API(API_ID) ON DELETE CASCADE,
    PRIMARY KEY (SUBSCRIPTION_ID)
)
;
--TODO: Have to add ON UPDATE CASCADE for the FOREIGN KEY(APPLICATION_ID) and FOREIGN KEY(API_ID) relations
CREATE TABLE AM_SUBSCRIPTION_KEY_MAPPING (
    SUBSCRIPTION_ID INTEGER,
    ACCESS_TOKEN VARCHAR(512),
    KEY_TYPE VARCHAR(512) NOT NULL,
    FOREIGN KEY(SUBSCRIPTION_ID) REFERENCES AM_SUBSCRIPTION(SUBSCRIPTION_ID) ON DELETE CASCADE,
    PRIMARY KEY(SUBSCRIPTION_ID,ACCESS_TOKEN)
)
;
--TODO: Have to add ON UPDATE CASCADE for the FOREIGN KEY(APPLICATION_ID) relation
CREATE TABLE AM_APPLICATION_KEY_MAPPING (
    APPLICATION_ID INTEGER,
    CONSUMER_KEY VARCHAR(512),
    KEY_TYPE VARCHAR(512) NOT NULL,
    FOREIGN KEY(APPLICATION_ID) REFERENCES AM_APPLICATION(APPLICATION_ID) ON DELETE CASCADE,
    PRIMARY KEY(APPLICATION_ID,CONSUMER_KEY)
)
;
--TODO: Have to add ON UPDATE CASCADE for the FOREIGN KEY(API_ID) relation
CREATE SEQUENCE AM_API_LC_EVENT_SEQUENCE START WITH 1 INCREMENT BY 1;
CREATE TABLE AM_API_LC_EVENT (
    EVENT_ID INTEGER DEFAULT nextval('am_api_lc_event_sequence'),
    API_ID INTEGER NOT NULL,
    PREVIOUS_STATE VARCHAR(50),
    NEW_STATE VARCHAR(50) NOT NULL,
    USER_ID VARCHAR(50) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    EVENT_DATE DATE NOT NULL,
    FOREIGN KEY(API_ID) REFERENCES AM_API(API_ID) ON DELETE CASCADE,
    PRIMARY KEY (EVENT_ID)
)
;

CREATE INDEX IDX_SUB_APP_ID ON AM_SUBSCRIPTION (APPLICATION_ID, SUBSCRIPTION_ID)
;
commit;
