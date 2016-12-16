package me.arcticlight.cs.databases

import me.arcticlight.animations.ScalaTween.Animatable

/**
  * Created by Max on 1/29/2016.
  */
class AnimationManager {
  private var animationList: Seq[ManagedAnimation] = Seq()
  private var myTime: Float = 0

  def isAnimating = animationList.nonEmpty

  def trigger(target: Animatable, onStart: ()=>Unit = null, onEnd: ()=>Unit = null): Unit = {
    animationList = animationList :+ ManagedAnimation(Some(target), myTime, onStart=onStart, onEnd=onEnd)
    target.seekTo(0)
  }

  def triggerNoDups(target: Animatable, onStart: ()=>Unit = null, onEnd: ()=>Unit = null): Unit = {
    animationList = animationList.filterNot(x => x.target.isDefined && x.target.get.equals(target))
    trigger(target, onStart, onEnd)
  }

  def triggerDelay(target: Animatable, delay: Float, onStart: ()=>Unit = null, onEnd: ()=>Unit = null): Unit = {
    animationList = animationList :+ ManagedAnimation(Some(target), myTime + delay, onStart=onStart, onEnd=onEnd)
  }

  def triggerDelayNoDups(target: Animatable, delay: Float, onStart: ()=>Unit = null, onEnd: ()=>Unit = null): Unit = {
    animationList = animationList.filterNot(_.target.contains(target))
    triggerDelay(target, delay, onStart, onEnd)
  }

  def setTimeout(target: () => Unit, delay: Float): Unit = {
    animationList = animationList :+ ManagedAnimation(None, myTime + delay, autoremove=true, target)
  }

  def killAllAnimations(): Unit = {
    animationList.foreach {_.target.foreach{_.seekTo(0)}}
    animationList = Seq()
  }

  def update(utime: Float): Unit = {
    if(utime <= 0) throw new Exception("AnimationManager does not support backwards time!")
    val oldTime = myTime
    myTime += utime
    val deadList: Seq[Option[ManagedAnimation]] = for(anim <- animationList) yield {
      if (anim.startTime < myTime) {
        anim.target match {
          case Some(target) =>
            if(anim.startTime + target.duration <= myTime) {
              target.seekTo(target.duration)
              if(anim.onEnd != null) anim.onEnd()
              if(anim.autoremove) Some(anim) else None
            } else {
              if(anim.onStart != null && anim.startTime < oldTime && !anim.started) {
                anim.onStart()
                anim.started = true
              }
              target.seekTo(myTime - anim.startTime)
              None
            }
          case None =>
            if(anim.onStart != null && !anim.started) {
              anim.onStart()
              anim.started = true
            }
            Some(anim)
        }
      }else {
        None
      }
    }
    animationList = animationList.diff(deadList.flatten)
  }

  class ManagedAnimation(val target: Option[Animatable],
                         val startTime: Float,
                         val autoremove: Boolean = true,
                         val onStart: () => Unit = null,
                         val onEnd: () => Unit = null,
                         var started: Boolean = false) {
    override def equals(obj: scala.Any): Boolean =
      obj match {
        case v: ManagedAnimation =>
          this.target match {
            case Some(x) =>
              x.equals(v.target.orNull)
            case None => v.target.isEmpty
          }
        case _ => false
      }
  }

  object ManagedAnimation {
    def apply(target: Option[Animatable],
              startTime: Float,
              autoremove: Boolean = true,
              onStart: () => Unit = null,
              onEnd: () => Unit = null): ManagedAnimation = {
      new ManagedAnimation(target,startTime,autoremove,onStart,onEnd)
    }
  }

}
