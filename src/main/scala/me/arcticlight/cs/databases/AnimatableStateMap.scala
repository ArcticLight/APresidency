package me.arcticlight.cs.databases

import me.arcticlight.animations.Eases
import me.arcticlight.animations.ScalaTween.{Animatable, AnimationTarget, ParTimeline, SeqTimeline, Tween}
import me.arcticlight.animations.ScalaTween.DefaultInterpolations._
import me.arcticlight.cs.databases.Animatables.Color
import me.arcticlight.cs.databases.Animatables.StockAnimations.TDelay
import processing.core.{PGraphics, PShape}

/**
  * Created by bogos on 11/12/2016.
  */
class AnimatableStateMap(val width: AnimationTarget[Float], val height: AnimationTarget[Float], svg: PShape) {
  private val states = Elections.getStates

  var stateColors: Seq[AnimationTarget[Color]] = {
    makeStateColors(1956).map(x=>AnimationTarget(x))
  }

  def makeStateColors(year: Int): Seq[Color] = {
    val v = Elections.getElectionsForYear(year)
    Seq.range(0, 51).map(x=>{
      v.find(z=>z.stateName == states(x).name) match {
        case Some(state) =>
          if(state.evDemocrat == 0 && state.evRepublican == 0) Elections.ThirdPartyColor
          else if (state.evRepublican > 0 && state.evDemocrat == 0) Elections.RepublicanColor
          else if (state.evDemocrat > 0 && state.evRepublican == 0) Elections.DemocratColor
          else Elections.TieColor
        case None => Elections.NoColor//Color(128,200,140)
      }
    })
  }

  def makeRippleDelayMap(x: Float, y: Float): Seq[Float] = {
    states.map(state=>{
      val (stateX, stateY) = unprojectVirtualPoint(state.xpos, state.ypos)
      val a = stateX - x
      val b = stateY - y
      (java.lang.Math.sqrt(a*a + b*b)/java.lang.Math.sqrt(width*width+height*height)).toFloat*0.8f
    })
  }

  def animateIntoElectionYear(year: Int, direction: Boolean = false): Animatable = {
    val delayMap = if(!direction) makeRippleDelayMap(width*1.25f, height/4)
      else makeRippleDelayMap(-width/4, height/2f)

    val myColors = if(direction) makeStateColors(year + 4) else makeStateColors(year - 4)
    val oldColors = makeStateColors(year)
    ParTimeline(
      stateColors.zip(myColors zip oldColors).zip(delayMap).map({case ((x,(a, b)),delay) =>
          SeqTimeline(
            TDelay(delay),
            Tween(x, a, b, 0.5f).ease(Eases.EaseInQuad)
          )
      }):_*
    )
  }

  def unprojectVirtualPoint(x: Float, y: Float): (Float, Float) =
    (x*0.655f*width,y*1.05f*height)
  def projectVirtualPoint(x: Float, y: Float): (Float, Float) =
    (x/(0.655f*width),y/1.05f/height)

  def draw(g: PGraphics): Unit = {
    g.background(255,255,255,0)
    g.shape(svg,0,0,width,height)
    g.loadPixels()
    for(i <- IndexedSeq.range(0,g.pixels.length)) {
      val x = g.pixels(i)
      if (100 <= g.red(x).toInt && g.red(x).toInt <= 150) {
        val c = stateColors(g.red(x).toInt - 100)
        g.pixels(i) = g.color(c.r, c.g, c.b, c.a)
      } else if (g.red(x) == 255) {
        g.pixels(i) = g.color(0,0,0,0)
      } else {
        g.pixels(i) = x
      }
    }
    g.updatePixels()
  }
}

object AnimatableStateMap {
  def apply(width: Float, height: Float, svg: PShape) = new AnimatableStateMap(AnimationTarget(width), AnimationTarget(height), svg)
}
