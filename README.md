CS380 Final Project
===================

This is Max's final project for CMPSC380.

To build the code in its entirity, you need:

1. Node.JS v7.1.0 and up
2. A working JDK8 and up (preferably Oracle)
3. SQLite3

To build on Linux, run the following in your terminal

```
$ node app.js > .data.csv          #Create the CSV datasheet from the raw data files
$ sqlite3 election_db.sqlite3 < populate_database.sql        #Populate the databases
$ ./gradlew :shadowJar        #Compile all the code
```

To run the code after it is built, you can do
```
$ java -jar build/libs/finalproject-1.0-SNAPSHOT-all.jar
```
which runs the compiled JAR, or you can alternatively just do `$ ./gradlew :run` but I can't
guarantee that the plain `:run` task will work correctly on all systems without additional
steps taken.

### About the visualization

What you see when you run the finished build is a comprehensive visualization of U.S. presidential
elections data, compiled from various sources but mostly from [Wikipedia](https://en.wikipedia.org)
and [The American Presidency Project](http://www.presidency.ucsb.edu/index.php). The actual map graphic
is an annotated SVG from Wikipedia, which is used under Fair Use for nonprofit, educational purposes.
The other graphics are generated procedurally.

You can page through the visualization by using the left and right arrow keys on your keyboard in order
to investigate the election results through the years. The visualization is designed to highlight the immense
disparity that exists between the results of the electoral and the popular vote in the presidential
election. You can see this disparity by comparing the left and right pie-charts. In a perfectly representative
election, the two pie charts should show almost exactly the same proportions.

Question Responses
==================

### 1. Data

The data that this project uses is real U.S. elections data, as mentioned previously. It has
been collected from multiple sources, and is the result of real U.S. elections.

### 2. Database Software

This project utilizes SQLite3, and the fully compiled database
can be browsed using SQBrowser if you would like to do that.

The data compiler is written in Javascript in order to ease the handling of JSON blob data,
hence why it is necessary to use Node.JS in order to compile the database. The visualization
uses a blend of Scala, Java, and Slick code.

### 3. Data Schema

Here is a copy of the database schema:
```SQL
CREATE TABLE stateTable (
    id                  INTEGER PRIMARY KEY,
    recolor             INTEGER NOT NULL UNIQUE,
    abbrev              VARCHAR(2) NOT NULL UNIQUE,
    name                VARCHAR(30) NOT NULL UNIQUE,
    xpos                NUMBER NOT NULL,
    ypos                NUMBER NOT NULL
);
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
CREATE TABLE electoralVotes (
    id                  INTEGER PRIMARY KEY,
    state               VARCHAR(30) NOT NULL,
    year                INTEGER NOT NULL,
    electoralVotes      INTEGER NOT NULL,
    FOREIGN KEY(state) REFERENCES stateTable(name)
);
CREATE TABLE electionRaces (
    year                INTEGER PRIMARY KEY,
    demCandidate        VARCHAR(80) NOT NULL,
    repCandidate        VARCHAR(80) NOT NULL,
    winner              TEXT(3) NOT NULL CHECK(winner in ('dem', 'rep'))
);
```

### 4. Source code

1. The raw data files are in CSV and JSON format, due to some peculiaraties
    associated with trying to build the tables from data available on the Web.
    It was simply easiest to manipulate the data in this format in between getting
    it from the Web and putting it into the SQLite3 database.
2. The application which changes the JSON format into CSV for SQLite to read
    is written in Javascript, and hence requires Node.js to run.
3. The data visualization is written in Scala, and depends on several third-party
    libraries to do its work. The animations are powered by a library of mine,
    [ScalaTween](https://github.com/ArcticLight/ScalaTween), the database interop
    is powered by [Slick](http://slick.lightbend.com/) and the 2D graphics are powered
    by [Processing](https://processing.org/)

### 5. Queries

The queries powering the visualization are found almost exclusively in the
`me.arcticlight.cs.databases.Elections` object, which houses methods which
fetch data from the database, as well as the database models for Slick.

Some examples include the following:

```scala
//get election candidates for a particular $year
db.run({for(e <- electionRaces if e.year === year) yield e}.result)

//get electoral votes for a $state and a $year
db.run({for(x <- electoralVotes if x.year === year) yield x}.result)
```

This Scala code is roughly equivalent to the following SQL:

```SQL
-- get election candidates for a particular year for some ${year}
select * from electionRaces where year == ${year};

-- get electoral votes for some ${stateName} and ${year}
select * from electoralVotes where state == ${stateName} and year == ${year};
```

### 6. Interpretation of Information from the Queries

Please see the comprehensive data visualization. (i.e. run the project code)

The main research question is to investigate just how unrepresentative the Electoral College
is when compared to the national popular vote. I personally believe that the complete data visualization
speaks for itself, but for the purposes of the lab report I will reiterate; the Electoral College
is **extremely** unrepresentative of the popular vote. Particularly in certain years such as
1964, 1972, 1980 and 1984, the Electoral Collge result looks almost nothing like the popular vote.

Some may argue that the Electoral College is not meant to mirror the popular vote, and instead is supposed
to make sure that a prospective president must win the most states. But this is also inaccurate, as can
be seen by comparing the Electoral College results with the state map. For example, in the year 2000,
George W. Bush won 31 states but had only just over half the Electoral Vote. If the Electoral College's
goal is to match the plurality of states that a candidate has won, it is **still** unrepresentative
according to this metric.

In short, the Electoral College is not a very good system for determining the presidency, as it is
very unrepresentative. It is unrepresentative of the number of states won by a candidate, and it is
also very unrepresentative of the popular vote.

Some other, miscellaneous general observations:

1. Republicans have won more presidential elections in the last 15 elections than Democrats.
2. The District of Columbia has voted consistently for the Democratic Party ever since it has had its own Electoral Vote.
    This is the only state which has never changed its vote. Even Nebraska, the closest runner up which has consistently
    voted Republican, has once given an electoral vote to a Democrat.
3. The Electoral College is hilariously unrepresentative of the popular vote.

### 7. Testing Your Database

You can manually verify that the election maps are similar to those graphics
at the American Presidency Project and at Wikipedia. Verification of the
pie charts can be done on a per-year basis by double checking the math
manually.

A cursory evaluation of the code will reveal that the visualization is using
the database as its primary data source, and changing the data in the database
is sufficient to change the visualization provided that you reload it.

This should be sufficient proof that the database and the associated visualization
is working as intended.

### 8. Interface

Please see the comprehensive data visualization. (i.e. run the project code)

Here is a screenshot:

![Interface Screenshot](https://bitbucket.org/ArcticLight/cs380f2016-clivem/raw/master/finalproject/screenshot.png)

