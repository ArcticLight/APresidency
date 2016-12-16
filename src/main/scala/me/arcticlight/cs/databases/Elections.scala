package me.arcticlight.cs.databases

import me.arcticlight.cs.databases.Animatables.Color
import me.arcticlight.cs.databases.Elections.Party.Party

import scala.languageFeature.postfixOps
import slick.driver.SQLiteDriver.api._
import slick.jdbc.GetResult

import scala.concurrent.Await
import scala.concurrent.duration._

object Elections {
  class States(tag: Tag) extends Table[State](tag, "stateTable") {
    def id = column[Int]("id", O.PrimaryKey)
    def recolor = column[Int]("recolor")
    def abbrev = column[String]("abbrev")
    def name = column[String]("name")
    def xpos = column[Float]("xpos")
    def ypos = column[Float]("ypos")
    def * = (id, recolor, abbrev, name, xpos, ypos) <> (State.tupled, State.unapply)
  }
  case class State(id: Int, recolor: Int, abbrev: String, name: String, xpos: Float, ypos: Float)
  val states = TableQuery[States]

  class ElectoralVotes(tag: Tag) extends Table[ElectoralVote](tag, "electoralVotes") {
    def id = column[Int]("id", O.PrimaryKey)
    def stateName = column[String]("state")
    def year = column[Int]("year")
    def electoralVotes = column[Int]("electoralVotes")
    def * = (id, stateName, year, electoralVotes) <> (ElectoralVote.tupled, ElectoralVote.unapply)

    def state = foreignKey("STATE_FK", stateName, states)(_.name)
  }
  case class ElectoralVote(id: Int, stateName: String, year: Int, electoralVotes: Int)
  val electoralVotes = TableQuery[ElectoralVotes]

  class ElectionRaces(tag: Tag) extends Table[PresidentialRace](tag, "electionRaces") {
    def year = column[Int]("year", O.PrimaryKey)
    def demCandidate = column[String]("demCandidate")
    def repCandidate = column[String]("repCandidate")
    def winner = column[String]("winner")

    /**
      * We know from the schema that 'winner' will only ever be "dem" or "rep". So we then cajole
      * this value into a special Enumeration. This requires handwriting the projection.
      */
    def * = (year, demCandidate, repCandidate, winner) <> ((x:Tuple4[Int,String,String,String]) => x match {
      case (a, b, c, d) => PresidentialRace(a, b, c, if(d == "dem") Party.Dem else Party.Rep)
    }, (x: PresidentialRace) => x match {
      case PresidentialRace(a,b,c,d) => Some((a,b,c, if(d == Party.Dem) "dem" else "rep"))
    })
  }
  object Party extends Enumeration {
    type Party = Value
    val Dem, Rep = Value
  }
  case class PresidentialRace(year:Int,demCandidate:String,repCandidate:String,winner:Party)
  val electionRaces = TableQuery[ElectionRaces]

  class Elections(tag: Tag) extends Table[Election](tag, "electionResults") {
    def id = column[Int]("id", O.PrimaryKey)
    def year = column[Int]("year")
    def stateName = column[String]("state")
    def totalVote = column[Int]("totalVote")
    def voteDemocrat = column[Int]("voteDemocrat")
    def voteRepublican = column[Int]("voteRepublican")
    def perVoteDemocrat = column[Double]("perVoteDemocrat")
    def perVoteRepublican = column[Double]("perVoteRepublican")
    def evDemocrat = column[Int]("evDemocrat")
    def evRepublican = column[Int]("evRepublican")

    def state = foreignKey("STATE_FK", stateName, states)(_.name)

    def * = (id, year, stateName, totalVote, voteDemocrat, voteRepublican,
      perVoteDemocrat, perVoteRepublican, evDemocrat, evRepublican) <> (Election.tupled, Election.unapply)
  }
  case class Election(id: Int, year: Int, stateName: String, totalVote: Int, voteDemocrat: Int, voteRepublican: Int,
                      perVoteDemocrat: Double, perVoteRepublican: Double, evDemocrat: Int, evRepublican: Int) {
    def state: State = getStateForName(this.stateName).get
  }
  val elections = TableQuery[Elections]

  implicit def getElectionResult = GetResult(r=>Election(r.nextInt, r.nextInt, r.<<,
    r.nextInt, r.nextInt, r.nextInt, r.nextDouble, r.nextDouble, r.nextInt, r.nextInt))

  private val db:Database = Database.forURL("jdbc:sqlite:election_db.sqlite3", driver="org.sqlite.JDBC")

  def getElectionYears: Seq[Int] = {
    Await.result(db.run({for(e <- elections) yield e.year}.result), Duration.Inf).distinct.sorted
  }

  def getElectionCandidates(year: Int): Option[PresidentialRace] = {
    Await.result(db.run({for(e <- electionRaces if e.year === year) yield e}.result), Duration.Inf).headOption
  }

  def getStates: Seq[State] = {
    Await.result(db.run({for(e <- states) yield e}.result), Duration.Inf)
  }

  def getElectoralVotes: Seq[ElectoralVote] = {
    Await.result(db.run({for(e <- electoralVotes) yield e}.result), Duration.Inf)
  }

  def getElectoralVotesForStateAndYear(state: String, year: Int): Option[ElectoralVote] = {
    Await.result(db.run({for(x <- electoralVotes if x.stateName === state && x.year === year) yield x}.result),
      Duration.Inf).headOption
  }

  def getElectoralVotesForYear(year: Int): Seq[ElectoralVote] = {
    Await.result(db.run({for(x <- electoralVotes if x.year === year) yield x}.result), Duration.Inf)
  }

  def getStateForName(stateName: String): Option[State] = {
    Await.result(db.run({for(e <- states if e.name === stateName) yield e}.result), Duration.Inf).headOption
  }

  def getElectionsForYear(year: Int): Seq[Election] = {
    Await.result(db.run({for(e <- elections if e.year === year) yield e}.result), Duration.Inf)
  }

  def getElectionsByStateName(state:String): Seq[Election] = {
    Await.result(db.run({for(e <- elections if e.stateName === state) yield e}.result), Duration.Inf)
  }

  val RepublicanColor = Color(204, 53, 53)
  val NoColor = Color(0,0,0,0)
  val DemocratColor = Color(42, 119, 201)
  val ThirdPartyColor = Color(128,200,140)
  val TieColor = Color(206, 65, 219)
}