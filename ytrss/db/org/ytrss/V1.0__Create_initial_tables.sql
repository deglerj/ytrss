CREATE TABLE "CHANNEL"
(
	"ID"				BIGINT			NOT NULL IDENTITY,
	"NAME"				LONGVARCHAR		NOT NULL UNIQUE,
	"URL"				LONGVARCHAR		NOT NULL,
	"SECURITY_TOKEN"	LONGVARCHAR 	NOT NULL UNIQUE,
	"INCLUDE_REGEX"		LONGVARCHAR,
	"EXCLUDE_REGEX"		LONGVARCHAR
);


CREATE TABLE "VIDEO"
(
	"ID"				BIGINT			NOT NULL IDENTITY,
	"CHANNEL_FK"		BIGINT			NOT NULL,
	"YOUT_ID"			LONGVARCHAR		NOT NULL,
	"NAME"				LONGVARCHAR		NOT NULL,
	"SECURITY_TOKEN"	LONGVARCHAR 	NOT NULL UNIQUE,
	"UPLOADED"			DATE			NOT NULL,
	"DISCOVERED"		TIMESTAMP		NOT NULL,
	"STATE"				INT				NOT NULL,
	"ERROR_MESSAGE"		LONGVARCHAR,
	"VIDEO_FILE"		LONGVARCHAR,
	"MP3_FILE"			LONGVARCHAR,
	
	CONSTRAINT "VIDEO_CHANNEL_FK"	FOREIGN KEY ("CHANNEL_FK")	REFERENCES "CHANNEL" ("ID")
);

CREATE INDEX "VIDEO_CHANNEL"	ON "VIDEO" ("CHANNEL_FK");