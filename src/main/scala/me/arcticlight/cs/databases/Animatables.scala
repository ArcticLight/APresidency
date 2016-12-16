package me.arcticlight.cs.databases

import me.arcticlight.animations.ScalaTween._
import me.arcticlight.animations.ScalaTween.DefaultInterpolations._
import processing.core.PVector

/**
  * Created by bogos on 11/11/2016.
  */
object Animatables {
  private def clamp255(x: Int): Int = x match {
    case _ if x < 0 => 0
    case _ if x > 255 => 255
    case _ => x
  }
  private def clamp255(x: Float): Int = x match {
    case _ if x < 0 => 0
    case _ if x > 255 => 255
    case _ => x.toInt
  }

  def clamp(x: Float, min: Float, max: Float) = x match {
    case _ if x < min => min
    case _ if x > max => max
    case _ => x
  }

  implicit object PVectorIsVectorLike extends VectorLike[PVector] {
    override def mult(a: PVector, b: Float): PVector = new PVector(a.x * b, a.y * b)
    override def add(a: PVector, b: PVector): PVector = new PVector(a.x + b.x, a.y + b.y)
  }

  implicit object SimpleStringTween extends Interpolatable[String] {
    override def interp(a: String, b: String, much: Float): String = if(much <= 0.99) a else b
  }

  case class Color(r: Int, g: Int, b: Int, a: Int = 255)

  /**
    * Companion object for Color, enabling <code>Color(255)</code> type syntax
    * which is grayscale like Processing's color parameter options.
    */
  object Color {
    def apply(i: Int): Color = Color(i,i,i,255)
  }

  implicit object ColorIsVectorLike extends VectorLike[Color] {
    override def mult(a: Color, b: Float): Color =
      Color(
        clamp255(a.r*b).toInt,
        clamp255(a.g*b).toInt,
        clamp255(a.b*b).toInt,
        clamp255(a.a*b).toInt
      )

    override def add(a: Color, b: Color): Color =
      Color(
        clamp255(a.r+b.r),
        clamp255(a.g+b.g),
        clamp255(a.b+b.b),
        clamp255(a.a+b.a)
      )

    override def interp(a: Color, b: Color, much: Float): Color =
      Color(
        clamp255(a.r + (b.r-a.r) * much),
        clamp255(a.g + (b.g-a.g) * much),
        clamp255(a.b + (b.b-a.b) * much),
        clamp255(a.a + (b.a-a.a) * much)
      )
  }

  object StockAnimations {
    case class TDelay(length: Float) extends Tween(AnimationTarget(0), 0, 0, length) {
      override def seekTo(utime: Float): Unit = {}
      override val duration: Float = length
    }

    def easeBackOut(amt: Float = 1.2f)(f: Float): Float = {
      val t = f - 1
      Math.pow(t,2).toFloat * ((amt + 1) * t + amt) + 1
    }

    def applyRipple(rippleSpeed: Float, parts: Animatable*): Animatable = {
      ParTimeline(
        parts(0) +:
          parts.drop(1).zipWithIndex.map({ case(x,i) =>
            SeqTimeline(
              TDelay(rippleSpeed*(i+1)),
              x
            )
          }) : _*
      )
    }

    private def clamp(f: Float, min: Float, max: Float): Float = f match {
      case _ if f <= min => min
      case _ if f >= max => max
      case _ => f
    }

    /**
      * A [[Tween]] with NO CLAMPING PROTECTION over its ease function.
      * Allows, for example, easeBackOut to work correctly.
      */
    class LeakyEaseTween[T: VectorLike](override val target: AnimationTarget[T],
                                      override val start: T,
                                      override val end: T,
                                      val ease: (Float) => Float) extends
      Tween[T](target, start, end) {
      override def seekTo(utime: Float): Unit = {
        target.target = implicitly[VectorLike[T]].interp(start, end, ease({
          if(utime <= 0) 0
          else if (utime >= duration) 1
          else utime/duration
        }))
      }
    }

    object LeakyEaseTween {
      def apply[T: VectorLike](target: AnimationTarget[T],
                             start: T,
                             end: T,
                             ease: (Float) => Float,
                             cycleDuration: Float = 1,
                             cycles: Int = 1) = new LeakyEaseTween(target, start, end, ease)
    }
  }
}
