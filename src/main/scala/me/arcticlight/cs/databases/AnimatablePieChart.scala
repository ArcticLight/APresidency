package me.arcticlight.cs.databases

import me.arcticlight.animations.Eases

import scala.languageFeature.implicitConversions
import me.arcticlight.animations.ScalaTween._
import me.arcticlight.cs.databases.Animatables.PVectorIsVectorLike
import me.arcticlight.animations.ScalaTween.DefaultInterpolations._
import me.arcticlight.cs.databases.Animatables.Color
import processing.core.{PConstants, PGraphics, PVector}

import scala.language.implicitConversions

object AnimatablePieChart {
  case class Datum(data: AnimationTarget[Float], color: AnimationTarget[Color])

  def apply(data: Seq[Float], color: Seq[Color], x: Float, y: Float, r: Float): AnimatablePieChart = {
    require(data.length == color.length)
    new AnimatablePieChart(data.zip(color).map {case (z,c) => Datum(AnimationTarget(z),AnimationTarget(c))},
      AnimationTarget(new PVector(x,y)),
      AnimationTarget(r),
      AnimationTarget(r)
    )
  }
}

class AnimatablePieChart(val data: Seq[AnimatablePieChart.Datum],
                         val pos: AnimationTarget[PVector],
                         val width: AnimationTarget[Float],
                         val height: AnimationTarget[Float]) {
  def getPortionList: Seq[((Float, Float), Color)] = {
    val sum = data.map(x=>x.data.target).sum
    val z = data.scanLeft(0f)({case (total, x) => total + (x.data/sum)})
    (z zip z.tail) zip data.map(x=>x.color.target)
  }

  def draw(g: PGraphics): Unit = {
    g.ellipseMode(PConstants.CENTER)
    g.pushMatrix()
    g.translate(pos.x, pos.y)
    g.rotate(-PConstants.HALF_PI)
    g.strokeWeight(1f)
    g.noStroke()
    getPortionList.reverse.foreach {case ((start,stop), color) =>
        import PConstants._
        import Animatables.clamp
        g.fill(color.r,color.g,color.b,color.a)
        g.arc(0,0,width,height,clamp(start*TWO_PI, 0, TWO_PI),clamp(stop*TWO_PI, 0, TWO_PI),PIE)
    }
    g.popMatrix()
  }

  def morphToMatchData(newData: Seq[Float]): Animatable = {
    morphFromToData(data.map(x=>x.data.target), newData)
  }

  def morphFromToData(oldData: Seq[Float], newData: Seq[Float]): Animatable = {
    require(newData.length == this.data.length && newData.length == oldData.length)

    ParTimeline(
      oldData.zip(newData).zip(data) map {case ((from,to), t) => Tween(t.data, from, to, 0.8f).ease(Eases.EaseOutQuad)}
        :_*
    )
  }
}

