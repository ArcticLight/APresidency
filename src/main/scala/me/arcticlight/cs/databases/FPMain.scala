package me.arcticlight.cs.databases

import me.arcticlight.animations.Eases
import me.arcticlight.animations.ScalaTween.{Animatable, ParTimeline, SeqTimeline, Tween}
import me.arcticlight.cs.databases.Animatables.{Color, StockAnimations}
import me.arcticlight.cs.databases.Animatables.PVectorIsVectorLike
import me.arcticlight.cs.databases.Animatables.SimpleStringTween
import me.arcticlight.cs.databases.Elections.Party
import processing.core.{PApplet, PFont, PGraphics, PVector}
import processing.core.PConstants._
import processing.event.KeyEvent

object FPMain {
  def main(args: Array[String]): Unit = {
    PApplet.main(Array("me.arcticlight.cs.databases.FPMain"))
  }
}

class FPMain extends PApplet {
  val years = Elections.getElectionYears
  val m = new AnimationManager

  lazy val popularVote = {
    val eResult = Elections.getElectionsForYear(years.min)
    val pve = {
      val a = eResult.map(x => x.voteDemocrat).sum.toFloat
      val b = eResult.map(x => x.voteRepublican).sum.toFloat
      val c = eResult.map(x => x.totalVote).sum - (a + b)
      Seq(b,a,c)
    }
    val colors = Seq(Elections.RepublicanColor, Elections.DemocratColor, Elections.ThirdPartyColor)

    AnimatablePieChart(pve, colors, width*5f/8f, height*1/8f, height/2f*0.3f)
  }

  lazy val electoralVote = {
    val eResult = Elections.getElectionsForYear(years.min)
    val xResult = Elections.getElectoralVotesForYear(years.min)
    val eve = {
      val a = eResult.map(x => x.evDemocrat).sum.toFloat
      val b = eResult.map(x => x.evRepublican).sum.toFloat
      val c = xResult.map(x => x.electoralVotes).sum - (a + b)
      Seq(b,a,c)
    }

    val colors = Seq(Elections.RepublicanColor, Elections.DemocratColor, Elections.ThirdPartyColor)

    AnimatablePieChart(eve, colors, width*7f/8f, height*1/8f, height/2f*0.3f)
  }

  var year: Int = years.min
  var statePad: PGraphics = _
  var otherPad: PGraphics = _
  var stateMap: AnimatableStateMap = _
  var oldTime = 0f
  var prevYearLabel: FlyingText = _
  var currentYearLabel: FlyingText = _
  var demLabel: FlyingText = _
  var repLabel: FlyingText = _
  var font: PFont = _
  var allTransitions: SeqTimeline = _

  def loserHeight = height*(3f/8f + 1f/16f)
  def winnerHeight = height*(3f/8f - 1f/16f) - 4f
  def demX = width*41f/64f
  def repX = width*55f/64f

  val iScale = 1

  override def settings(): Unit = {
    size(1280*iScale,800*iScale,P2D)
  }

  override def setup(): Unit = {
    statePad = createGraphics(1280/2, 800/2)
    statePad.noSmooth()
    stateMap = AnimatableStateMap(1280/2, 800/2, loadShape("Blank_US_Map.svg"))
    otherPad = createGraphics(width,height)
    font = loadFont("Calibri-48.vlw")

    prevYearLabel = FlyingText("???", width*3f/4f, height/2f, Color(0,0,0,0))
    currentYearLabel = FlyingText(String.valueOf(year), width*3f/4f, height/2f, Color(0))

    val candidates = Elections.getElectionCandidates(year).get
    demLabel = FlyingText(candidates.demCandidate, demX,
      if(candidates.winner == Party.Dem) winnerHeight else loserHeight, Elections.DemocratColor)
    repLabel = FlyingText(candidates.repCandidate, repX,
      if(candidates.winner == Party.Rep) winnerHeight else loserHeight, Elections.RepublicanColor)

    allTransitions = SeqTimeline(makeAllTransitions():_*)
  }

  override def draw(): Unit = {
    val newTime = millis()/700f - oldTime
    if(newTime < 0) throw new Exception("This should never happen! Time is running backwards!")
    m.update(newTime)
    //allTransitions.seekTo((mouseX.toFloat/width.toFloat)*allTransitions.duration)
    background(249, 249, 244)
    statePad.beginDraw()
    statePad.background(0,0,0,0)
    stateMap.draw(statePad)
    statePad.endDraw()
    image(statePad, width*0.03f, height*0.03f, width/2, height/2)

    otherPad.beginDraw()
    otherPad.background(0,0,0,0)
    otherPad.textFont(font)
    otherPad.textAlign(CENTER,TOP)
    otherPad.textSize(48f * iScale)
    prevYearLabel.draw(otherPad)
    currentYearLabel.draw(otherPad)
    popularVote.draw(otherPad)
    otherPad.textSize(18f * iScale)
    otherPad.textAlign(CENTER,TOP)
    otherPad.fill(0)
    otherPad.text("Popular Vote", popularVote.pos.x, popularVote.pos.y + 10 * iScale + popularVote.height/2f)
    electoralVote.draw(otherPad)
    otherPad.fill(0)
    otherPad.text("Electoral Vote", electoralVote.pos.x, electoralVote.pos.y + 10 * iScale + electoralVote.height/2f)
    otherPad.textSize(32f * iScale)
    otherPad.textAlign(CENTER,CENTER)
    demLabel.draw(otherPad)
    repLabel.draw(otherPad)
    otherPad.fill(0)
    otherPad.text("VS", width*3f/4f, height*3f/8f)
    var color = Elections.RepublicanColor
    otherPad.fill(color.r,color.g,color.b,color.a)
    val hpad = 28f * iScale
    val vpad = 7f * iScale
    otherPad.triangle(width*3f/4f + hpad,height*(3f/8f) - vpad, width*3f/4f + hpad, height*3f/8f + vpad, width*15f/16f + hpad, height*3f/8f)
    color = Elections.DemocratColor
    otherPad.fill(color.r,color.g,color.b,color.a)
    otherPad.triangle(width*3f/4f - hpad,height*(3f/8f) - vpad, width*3f/4f - hpad, height*3f/8f + vpad, width*9f/16f - hpad, height*3f/8f)
    otherPad.endDraw()
    image(otherPad, 0, 0)

    textFont(font)
    textSize(21f * iScale)
    rectMode(CORNER)
    textAlign(LEFT,TOP)
    noStroke()
    color = Elections.RepublicanColor
    fill(color.r,color.g,color.b,color.a)
    val rectSize = 18f * iScale
    rect(width*0.1f,height*0.6f,-rectSize, rectSize)
    fill(0)
    text("Electoral Vote Republican", width*0.1f + 6 * iScale, height*0.6f + 1 * iScale)
    color = Elections.DemocratColor
    fill(color.r,color.g,color.b,color.a)
    rect(width*0.33f, height*0.6f,-rectSize, rectSize)
    fill(0)
    text("Electoral Vote Democrat", width*0.33f + 6 * iScale, height*0.6f + 1 * iScale)
    color = Elections.ThirdPartyColor
    fill(color.r,color.g,color.b,color.a)
    rect(width*0.1f, height*0.6f + 28 * iScale, -rectSize, rectSize)
    fill(0)
    text("Electoral Vote Third-Party", width*0.1f + 6 * iScale, height*0.6f + 29 * iScale)
    color = Elections.TieColor
    fill(color.r, color.g, color.b, color.a)
    rect(width*0.33f, height*0.6f + 28 * iScale,-rectSize, rectSize)
    fill(0)
    text("Electoral Vote Split", width*0.33f + 6 * iScale, height*0.6f + 29 * iScale)

    fill(0)
    textAlign(CENTER, CENTER)
    textSize(48f*iScale)
    text("Unrepresentativeness: " + zeroPad(calcUnrepresentativeness().toString) + "%", width*0.75f, height*0.6f + 20 * iScale)

    oldTime += newTime
  }

  override def keyPressed(event: KeyEvent): Unit = {
    if(!m.isAnimating) {
      if (event.getKeyCode == LEFT && year > years.min) {
        year -= 4
        triggerYearChange(true)
      }
      else if (event.getKeyCode == RIGHT && year < years.max) {
        year += 4
        triggerYearChange(false)
      }
    }
  }

  def zeroPad(x: String): String = if(x.length < 2) zeroPad("0" + x) else x

  def calcUnrepresentativeness(): Int = {
    val popularTotal = popularVote.data.map(_.data.target).sum
    val electoralTotal = electoralVote.data.map(_.data.target).sum
    val pPercentages = popularVote.data.map(_.data.target).map(x=>x/popularTotal*100)
    val ePercentages = electoralVote.data.map(_.data.target).map(x=>x/electoralTotal*100)
    pPercentages.zip(ePercentages).map({
      case (popular, electoral) =>
        Math.abs(popular-electoral).toInt
    }).sum
  }

  def makeAllTransitions(): Seq[Animatable] =
    years.tail.map(x=>makeTransition(x, direction=false))

  def triggerYearChange(direction: Boolean): Unit = {
    m.trigger(makeTransition(year, direction))
  }

  def makeTransition(year: Int, direction: Boolean): Animatable = {
    val prevYear = if(direction) year + 4 else year - 4

    val lastCandidates = Elections.getElectionCandidates(prevYear).get
    val thisCandidates = Elections.getElectionCandidates(year).get
    val previousDemPos = new PVector(demX, if(lastCandidates.winner == Party.Dem) winnerHeight else loserHeight)
    val previousRepPos = new PVector(repX, if(lastCandidates.winner == Party.Rep) winnerHeight else loserHeight)
    val nextDemPos = new PVector(demX, if(thisCandidates.winner == Party.Dem) winnerHeight else loserHeight)
    val nextRepPos = new PVector(repX, if(thisCandidates.winner == Party.Rep) winnerHeight else loserHeight)

    val textTransition = if(direction) {
      ParTimeline(
        Tween(prevYearLabel.position, new PVector(width*3f/4f, height/2f), new PVector(width, height/2f), .8f)
          .ease(Eases.EaseInQuad),
        Tween(currentYearLabel.position, new PVector(width/2f, height/2f), new PVector(width*3f/4f, height/2f), .8f)
          .ease(Eases.EaseOutQuad),
        Tween(prevYearLabel.s, String.valueOf(prevYear), "???", 0.8f),
        Tween(currentYearLabel.s, String.valueOf(year), String.valueOf(year), 0.8f),
        Tween(prevYearLabel.color, Color(0), Color(0,0,0,0), 0.8f),
        Tween(currentYearLabel.color, Color(0,0,0,0), Color(0), 0.8f),
        ParTimeline(
          SeqTimeline(
            ParTimeline(
              Seq(Some(Tween(demLabel.s, lastCandidates.demCandidate, thisCandidates.demCandidate, 0.4f)),
                if(lastCandidates.demCandidate != thisCandidates.demCandidate || lastCandidates.winner != thisCandidates.winner)
                  Some(Tween(demLabel.color, Elections.DemocratColor, Color(0,0,0,0), 0.2f))
                else None,
                Some(Tween(repLabel.s, lastCandidates.repCandidate, thisCandidates.repCandidate, 0.4f)),
                if(lastCandidates.repCandidate != thisCandidates.repCandidate || lastCandidates.winner != thisCandidates.winner)
                  Some(Tween(repLabel.color, Elections.RepublicanColor, Color(0,0,0,0), 0.2f))
                else None).flatten :_*
            ),
            ParTimeline(
              Seq(Some(Tween(demLabel.s, thisCandidates.demCandidate, thisCandidates.demCandidate, 0.4f)),
                if(lastCandidates.demCandidate != thisCandidates.demCandidate || lastCandidates.winner != thisCandidates.winner)
                  Some(Tween(demLabel.color, Color(0,0,0,0), Elections.DemocratColor, 0.4f))
                else None,
                Some(Tween(repLabel.s, thisCandidates.repCandidate, thisCandidates.repCandidate, 0.4f)),
                if(lastCandidates.repCandidate != thisCandidates.repCandidate || lastCandidates.winner != thisCandidates.winner)
                  Some(Tween(repLabel.color, Color(0,0,0,0), Elections.RepublicanColor, 0.4f))
                else None).flatten :_*
            )
          ).ease(Eases.EaseInQuad),
          Tween(demLabel.position, previousDemPos, nextDemPos, 0.8f).ease(Eases.EaseInQuad),
          Tween(repLabel.position, previousRepPos, nextRepPos, 0.8f).ease(Eases.EaseInQuad)
        )
      )
    } else {
      ParTimeline(
        Tween(prevYearLabel.position, new PVector(width*3f/4f, height/2f), new PVector(width/2f, height/2f), .8f)
            .ease(Eases.EaseInQuad),
        Tween(currentYearLabel.position, new PVector(width, height/2f), new PVector(width*3f/4f, height/2f), .8f)
            .ease(Eases.EaseOutQuad),
        Tween(prevYearLabel.s, String.valueOf(prevYear), "???", 0.8f),
        Tween(currentYearLabel.s, String.valueOf(year), String.valueOf(year), 0.8f),
        Tween(prevYearLabel.color, Color(0), Color(0,0,0,0), 0.8f),
        Tween(currentYearLabel.color, Color(0,0,0,0), Color(0), 0.8f),
        ParTimeline(
          SeqTimeline(
            ParTimeline(
              Seq(Some(Tween(demLabel.s, lastCandidates.demCandidate, thisCandidates.demCandidate, 0.4f)),
                if(lastCandidates.demCandidate != thisCandidates.demCandidate || lastCandidates.winner != thisCandidates.winner)
                  Some(Tween(demLabel.color, Elections.DemocratColor, Color(0,0,0,0), 0.2f))
                else None,
                Some(Tween(repLabel.s, lastCandidates.repCandidate, thisCandidates.repCandidate, 0.4f)),
                if(lastCandidates.repCandidate != thisCandidates.repCandidate || lastCandidates.winner != thisCandidates.winner)
                  Some(Tween(repLabel.color, Elections.RepublicanColor, Color(0,0,0,0), 0.2f))
                else None).flatten :_*
            ),
            ParTimeline(
              Seq(Some(Tween(demLabel.s, thisCandidates.demCandidate, thisCandidates.demCandidate, 0.4f)),
                if(lastCandidates.demCandidate != thisCandidates.demCandidate || lastCandidates.winner != thisCandidates.winner)
                  Some(Tween(demLabel.color, Color(0,0,0,0), Elections.DemocratColor, 0.4f))
                else None,
                Some(Tween(repLabel.s, thisCandidates.repCandidate, thisCandidates.repCandidate, 0.4f)),
                if(lastCandidates.repCandidate != thisCandidates.repCandidate || lastCandidates.winner != thisCandidates.winner)
                  Some(Tween(repLabel.color, Color(0,0,0,0), Elections.RepublicanColor, 0.4f))
                else None).flatten :_*
            )
          ).ease(Eases.EaseInQuad),
          Tween(demLabel.position, previousDemPos, nextDemPos, 0.8f).ease(Eases.EaseInQuad),
          Tween(repLabel.position, previousRepPos, nextRepPos, 0.8f).ease(Eases.EaseInQuad)
        )
      )
    }

    val eResult = Elections.getElectionsForYear(year)
    val pve = {
      val a = eResult.map(x => x.voteDemocrat).sum.toFloat
      val b = eResult.map(x => x.voteRepublican).sum.toFloat
      val c = eResult.map(x => x.totalVote).sum - (a + b)
      Seq(b,a,c)
    }

    val prevEResult = Elections.getElectionsForYear(prevYear)
    val prevPve = {
      val a = prevEResult.map(x => x.voteDemocrat).sum.toFloat
      val b = prevEResult.map(x => x.voteRepublican).sum.toFloat
      val c = prevEResult.map(x => x.totalVote).sum - (a + b)
      Seq(b,a,c)
    }

    val xResult = Elections.getElectoralVotesForYear(years.min)
    val eve = {
      val a = eResult.map(x => x.evDemocrat).sum.toFloat
      val b = eResult.map(x => x.evRepublican).sum.toFloat
      val c = xResult.map(x => x.electoralVotes).sum - (a + b)
      Seq(b,a,c)
    }

    val prevXResult = Elections.getElectoralVotesForYear(years.min)
    val prevEve = {
      val a = prevEResult.map(x => x.evDemocrat).sum.toFloat
      val b = prevEResult.map(x => x.evRepublican).sum.toFloat
      val c = prevXResult.map(x => x.electoralVotes).sum - (a + b)
      Seq(b,a,c)
    }

    val q = Seq(
      stateMap.animateIntoElectionYear(year, direction),
      ParTimeline(
        textTransition,
        popularVote.morphFromToData(prevPve, pve),
        electoralVote.morphFromToData(prevEve, eve)
      )
    )

    if(direction) StockAnimations.applyRipple(0.9f, q:_*)
    else StockAnimations.applyRipple(0.1f, q.reverse:_*)
  }
}