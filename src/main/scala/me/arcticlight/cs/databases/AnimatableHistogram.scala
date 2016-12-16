package me.arcticlight.cs.databases

import me.arcticlight.animations.Eases
import me.arcticlight.animations.ScalaTween.DefaultInterpolations._
import me.arcticlight.animations.ScalaTween._
import me.arcticlight.cs.databases.Animatables.Color
import processing.core.{PApplet, PConstants, PGraphics}

/**
  * Created by bogos on 11/11/2016.
  */
class AnimatableHistogram(val data: Seq[AnimationTarget[Float]],
                          val width: Float,
                          val height: Float,
                          val interPadding: Float = 0,
                          val colors: Seq[AnimationTarget[Color]]) {
  val baseWidth = AnimationTarget(0.0f)
  val columnHeights = data.map({x => new AnimationTarget(0.0f)})
  def maxColHeight = data.map(_.target).max

  val introAnimation = ParTimeline(
    Tween(baseWidth, 0f, width, 0.8f).ease(Eases.EaseOutQuad),
    SeqTimeline(
      Tween(AnimationTarget(0f), 0f, 0f, 0.7f),
      ParTimeline(
        columnHeights.zipWithIndex.map({case (x,i)=>Tween(x, 0f, (data(i)/maxColHeight)*height, 0.3f)}):_*
      ).ease(Eases.EaseOutQuad)
    )
  )

  def draw(g: PGraphics): Unit = {
    import g._
    noStroke()
    val cwidth = this.width/data.length
    rectMode(PConstants.CORNER)
    columnHeights.zipWithIndex.foreach({case (x,i) =>
        fill(colors(i).r, colors(i).g, colors(i).b, colors(i).a)
        val cx = cwidth * i
        rect(cx + interPadding, this.height, cwidth - interPadding*2, 0-columnHeights(i))
    })
    stroke(0)
    line(0,this.height-1,baseWidth,this.height-1)
  }
}

object AnimatableHistogram {
  def apply(data: Seq[Float]) =
    new AnimatableHistogram(data.map(AnimationTarget(_)), 200,150,0,data.map(x=>AnimationTarget(Color(255,0,0))))
  def apply(data: Seq[Float], width: Float, height: Float) =
    new AnimatableHistogram(data.map(AnimationTarget(_)), width, height, 0, data.map(x=>AnimationTarget(Color(255,0,0))))
  def apply(data: Seq[Float], width: Float, height: Float, interPadding: Float) =
    new AnimatableHistogram(data.map(AnimationTarget(_)), width, height, interPadding, data.map(x=>AnimationTarget(Color(255,0,0))))
  def apply(data: Seq[Float], width: Float, height: Float, interPadding: Float, colors: Seq[Color]) =
    new AnimatableHistogram(data.map(AnimationTarget(_)), width, height, interPadding, colors.map(x=>AnimationTarget(x)))
}