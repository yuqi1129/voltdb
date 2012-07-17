CREATE TABLE TU1 (
	ID INTEGER NOT NULL,
	POINTS INTEGER NOT NULL,
	PRIMARY KEY (ID)
);
create unique index idx_U1_COUNTER on TU1 (POINTS);
	
CREATE TABLE TU2 (
	ID INTEGER NOT NULL,
	POINTS INTEGER NOT NULL,
	UNAME VARCHAR(10) NOT NULL,
	PRIMARY KEY (ID)
);
create unique index idx_U2_COUNTER on TU2 (UNAME,POINTS);

CREATE TABLE TU3 (
	ID INTEGER NOT NULL,
	POINTS INTEGER NOT NULL,
	TEL INTEGER NOT NULL,
	PRIMARY KEY (ID)
);
create unique index idx_U3_COUNTER on TU3 (TEL,POINTS);

CREATE TABLE TU4 (
	ID INTEGER NOT NULL,
	POINTS INTEGER NOT NULL,
	UNAME VARCHAR(10) NOT NULL,
	SEX TINYINT NOT NULL,
	PRIMARY KEY (ID)
);
create unique index idx_U4_COUNTER on TU4 (UNAME,SEX,POINTS);


CREATE TABLE TM1 (
	ID INTEGER NOT NULL,
	POINTS INTEGER NOT NULL,
	PRIMARY KEY (ID)
);
create index idx_M1_COUNTER on TM1 (POINTS);
	
CREATE TABLE TM2 (
	ID INTEGER NOT NULL,
	POINTS INTEGER NOT NULL,
	UNAME VARCHAR(10) NOT NULL,
	PRIMARY KEY (ID)
);
create index idx_M2_COUNTER on TM2 (UNAME,POINTS);

CREATE TABLE TM3 (
	ID INTEGER NOT NULL,
	POINTS INTEGER NOT NULL,
	TEL INTEGER NOT NULL,
	PRIMARY KEY (ID)
);
create index idx_M3_COUNTER on TM3 (TEL,POINTS);

CREATE TABLE TM4 (
	ID INTEGER NOT NULL,
	POINTS INTEGER NOT NULL,
	UNAME VARCHAR(10) NOT NULL,
	SEX TINYINT NOT NULL,
	PRIMARY KEY (ID)
);
create index idx_M4_COUNTER on TM4 (UNAME,SEX,POINTS);