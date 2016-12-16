package me.arcticlight.cs.databases

import me.arcticlight.animations.Eases
import me.arcticlight.animations.ScalaTween._
import me.arcticlight.cs.databases.Animatables._
import processing.core.{PGraphics, PVector}

/**
  * Created by bogos on 11/13/2016.
  */
class FlyingText(val s: AnimationTarget[String], val position: AnimationTarget[PVector], val color: AnimationTarget[Color] = AnimationTarget(Color(0))) {
  def width(g: PGraphics) = g.textWidth(s)

  def flyIn(bound: Float) = ParTimeline(
    Tween(position, new PVector(bound, position.y), position.target, .6f).ease(Eases.EaseOutQuad),
    Tween(color, Color(color.r, color.b, color.g, 0), Color(0), .5f)
  )

  def flyOut(bound: Float) = ParTimeline(
    Tween(position, position.target, new PVector(bound, position.y), .5f).ease(Eases.EaseInQuad),
    Tween(color, color.target, Color(color.r, color.g, color.b, 0), .5f)
  )

  def draw(g: PGraphics): Unit = {
    g.fill(color.r, color.g, color.b, color.a)
    g.text(s.target, position.x, position.y)
  }
}

object FlyingText {
  def apply(s:String,x:Float,y: Float): FlyingText = new FlyingText(AnimationTarget(s), AnimationTarget(new PVector(x,y)))
  def apply(s:String,x:Float,y:Float,c:Color): FlyingText = new FlyingText(AnimationTarget(s),AnimationTarget(new PVector(x,y)),AnimationTarget(c))
}
