package com.backwards.persistence.ex4

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}

object RecoveryApp extends App {
  val system = ActorSystem("recovery")

  val recoveryActor = system.actorOf(Props[RecoveryActor], "recovery-actor")

  (1 to 1000) foreach { i =>
    recoveryActor ! Command(s"Command $i")
  }
}

class RecoveryActor extends PersistentActor with ActorLogging {
  def persistenceId: String = s"recovery-actor"

  def receiveCommand: Receive = {
    case Command(contents) =>
      persist(Event(contents)) { event =>
        log info s"Persisted $event; recovery is ${if (recoveryFinished) "" else "NOT"} finished"
      }
  }

  def receiveRecover: Receive = {
    case Event(contents) =>
      if (simulateRecoveryFailure && contents.contains("314"))
        throw new Exception("I can't handle 314")

      log info s"Recovered event with contents: $contents; recovery is ${if (recoveryFinished) "" else "NOT"} finished"

    case RecoveryCompleted =>
      // Additional intialisation could be performed here when recovery is indeed complete
      log info "Recovery is complete"
  }

  override protected def onRecoveryFailure(cause: Throwable, event: Option[Any]): Unit = {
    log error s"I failed at recovery: event = $event, cause = ${cause.getMessage}"
    super.onRecoveryFailure(cause, event)
  }

  /**
    * Customised recovery examples (potentially for debugging recovery issues)
    */
  // override def recovery: Recovery = Recovery(toSequenceNr = 100)
  // override def recovery: Recovery = Recovery(fromSnapshot = SnapshotSelectionCriteria.Latest)
  // override def recovery: Recovery = Recovery.none // Recovery will not take place

  def simulateRecoveryFailure: Boolean =
    (Option(System getProperty "SIMULATE_RECOVERY_FAILURE").map(_.toLowerCase) getOrElse "false") == "true"
}

final case class Command(contents: String)

final case class Event(contents: String)