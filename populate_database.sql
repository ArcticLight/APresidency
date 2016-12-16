PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;

DROP TABLE stateTable;
CREATE TABLE stateTable (
    id                  INTEGER PRIMARY KEY,
    recolor             INTEGER NOT NULL UNIQUE,
    abbrev              VARCHAR(2) NOT NULL UNIQUE,
    name                VARCHAR(30) NOT NULL UNIQUE,
    xpos                NUMBER NOT NULL,
    ypos                NUMBER NOT NULL
);

INSERT INTO "stateTable" values(0,100,'AL','Alabama',1.09,0.68);
INSERT INTO "stateTable" values(1,101,'AK','Alaska',0.2,0.82);
INSERT INTO "stateTable" values(2,102,'AZ','Arizona',0.33,0.60);
INSERT INTO "stateTable" values(3,103,'AR','Arkansas',0.91,0.62);
INSERT INTO "stateTable" values(4,104,'CA','California',0.12,0.47);
INSERT INTO "stateTable" values(5,105,'CO','Colorado',0.53,0.45);
INSERT INTO "stateTable" values(6,106,'CT','Connecticut',1.44,0.30);
INSERT INTO "stateTable" values(7,107,'DE','Delaware',1.38,0.40);
INSERT INTO "stateTable" values(8,108,'FL','Florida',1.27,0.83);
INSERT INTO "stateTable" values(9,109,'GA','Georgia',1.20,0.68);
INSERT INTO "stateTable" values(10,110,'HI','Hawaii',0.50,0.92);
INSERT INTO "stateTable" values(11,111,'ID','Idaho',0.31,0.22);
INSERT INTO "stateTable" values(12,112,'IL','Illinois',1.00,0.41);
INSERT INTO "stateTable" values(13,113,'IN','Indiana',1.08,0.42);
INSERT INTO "stateTable" values(14,114,'IA','Iowa',0.88,0.35);
INSERT INTO "stateTable" values(15,115,'KS','Kansas',0.74,0.48);
INSERT INTO "stateTable" values(16,116,'KY','Kentucky',1.12,0.50);
INSERT INTO "stateTable" values(17,117,'LA','Louisiana',0.92,0.75);
INSERT INTO "stateTable" values(18,118,'ME','Maine',1.50,0.15);
INSERT INTO "stateTable" values(19,119,'MD','Maryland',1.34,0.40);
INSERT INTO "stateTable" values(20,120,'MA','Massachusetts',1.46,0.27);
INSERT INTO "stateTable" values(21,121,'MI','Michigan',1.11,0.23);
INSERT INTO "stateTable" values(22,122,'MN','Minnesota',0.84,0.20);
INSERT INTO "stateTable" values(23,123,'MS','Mississippi',1.00,0.69);
INSERT INTO "stateTable" values(24,124,'MO','Missouri',0.91,0.49);
INSERT INTO "stateTable" values(25,125,'MT','Montana',0.46,0.14);
INSERT INTO "stateTable" values(26,126,'NE','Nebraska',0.70,0.37);
INSERT INTO "stateTable" values(27,127,'NV','Nevada',0.21,0.39);
INSERT INTO "stateTable" values(28,128,'NH','New Hampshire',1.45,0.22);
INSERT INTO "stateTable" values(29,129,'NJ','New Jersey',1.41,0.37);
INSERT INTO "stateTable" values(30,130,'NM','New Mexico',0.50,0.62);
INSERT INTO "stateTable" values(31,131,'NY','New York',1.35,0.26);
INSERT INTO "stateTable" values(32,132,'NC','North Carolina',1.31,0.55);
INSERT INTO "stateTable" values(33,133,'ND','North Dakota',0.70,0.15);
INSERT INTO "stateTable" values(34,134,'OH','Ohio',1.17,0.40);
INSERT INTO "stateTable" values(35,135,'OK','Oklahoma',0.77,0.60);
INSERT INTO "stateTable" values(36,136,'OR','Oregon',0.15,0.20);
INSERT INTO "stateTable" values(37,137,'PA','Pennsylvania',1.31,0.35);
INSERT INTO "stateTable" values(38,138,'RI','Rhode Island',1.46,0.29);
INSERT INTO "stateTable" values(39,139,'SC','South Carolina',1.27,0.63);
INSERT INTO "stateTable" values(40,140,'SD','South Dakota',0.69,0.26);
INSERT INTO "stateTable" values(41,141,'TN','Tennessee',1.10,0.57);
INSERT INTO "stateTable" values(42,142,'TX','Texas',0.71,0.74);
INSERT INTO "stateTable" values(43,143,'UT','Utah',0.36,0.42);
INSERT INTO "stateTable" values(44,144,'VT','Vermont',1.41,0.20);
INSERT INTO "stateTable" values(45,145,'VA','Virginia',1.31,0.47);
INSERT INTO "stateTable" values(46,146,'WA','Washington',0.19,0.08);
INSERT INTO "stateTable" values(47,147,'WV','West Virginia',1.24,0.45);
INSERT INTO "stateTable" values(48,148,'WI','Wisconsin',0.97,0.26);
INSERT INTO "stateTable" values(49,149,'WY','Wyoming',0.50,0.30);
INSERT INTO "stateTable" values(50,150,'DC','Dist of Col',1.342,0.416);
COMMIT;

PRAGMA foreign_keys=ON;

BEGIN TRANSACTION;
DROP TABLE electionResults;
CREATE TABLE electionResults (
    id                  INTEGER PRIMARY KEY,
    year                INTEGER NOT NULL,
    state               VARCHAR(30) NOT NULL,
    totalVote           INTEGER NOT NULL,
    voteDemocrat        INTEGER NOT NULL,
    voteRepublican      INTEGER NOT NULL,
    perVoteDemocrat     NUMBER NOT NULL,
    perVoteRepublican   NUMBER NOT NULL,
    evDemocrat          INTEGER NOT NULL,
    evRepublican        INTEGER NOT NULL,
    FOREIGN KEY(state) REFERENCES stateTable(name)
);

.mode csv
.import ./.data.csv electionResults
COMMIT;

BEGIN TRANSACTION;
DROP TABLE electoralVotes;
CREATE TABLE electoralVotes (
    id                  INTEGER PRIMARY KEY,
    state               VARCHAR(30) NOT NULL,
    year                INTEGER NOT NULL,
    electoralVotes      INTEGER NOT NULL,
    FOREIGN KEY(state) REFERENCES stateTable(name)
);

.mode csv
.import ./rawdata/electoral_vote_data_1956_2012.csv electoralVotes

COMMIT;

BEGIN TRANSACTION;
DROP TABLE electionRaces;
CREATE TABLE electionRaces (
    year                INTEGER PRIMARY KEY,
    demCandidate        VARCHAR(80) NOT NULL,
    repCandidate        VARCHAR(80) NOT NULL,
    winner              TEXT(3) NOT NULL CHECK(winner in ('dem', 'rep'))
);

INSERT INTO electionRaces VALUES(2012, 'Barack Obama', 'Mitt Romney', 'dem');
INSERT INTO electionRaces VALUES(2008, 'Barack Obama', 'John McCain', 'dem');
INSERT INTO electionRaces VALUES(2004, 'John Kerry', 'George W. Bush', 'rep');
INSERT INTO electionRaces VALUES(2000, 'Al Gore', 'George W. Bush', 'rep');
INSERT INTO electionRaces VALUES(1996, 'Bill Clinton', 'Robert Dole', 'dem');
INSERT INTO electionRaces VALUES(1992, 'Bill Clinton', 'George Bush', 'dem');
INSERT INTO electionRaces VALUES(1988, 'Michael S. Dukakis', 'George Bush', 'rep');
INSERT INTO electionRaces VALUES(1984, 'Walter Mondale', 'Ronald Reagan', 'rep');
INSERT INTO electionRaces VALUES(1980, 'Jimmy Carter', 'Ronald Reagan', 'rep');
INSERT INTO electionRaces VALUES(1976, 'Jimmy Carter', 'Gerald R. Ford', 'dem');
INSERT INTO electionRaces VALUES(1972, 'George McGovern', 'Richard M. Nixon', 'rep');
INSERT INTO electionRaces VALUES(1968, 'Hubert Humphrey', 'Richard M. Nixon', 'rep');
INSERT INTO electionRaces VALUES(1964, 'Lyndon B. Johnson', 'Barry Goldwater', 'dem');
INSERT INTO electionRaces VALUES(1960, 'John Kennedy', 'Richard M. Nixon', 'dem');
INSERT INTO electionRaces VALUES(1956, 'Adlai Stevenson', 'Dwight D. Eisenhower', 'rep');
COMMIT;

.header on
.mode column