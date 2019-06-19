package com.backwards.persistence.ex1

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import com.backwards.collection.MapOps._

/**
  * Persistent actor for a voting station.
  * Keep:
  * - The citizens who voted
  * - The poll - mapping between a candidate and the number of received votes
  * The actor must be able to recover its state if it's shut down or restarted.
  */
object PersistentActorsExercise extends App {
  val system = ActorSystem("persistent-actors-exercise")

  val votingStation = system.actorOf(Props[VotingStation], "voting-station")

  votingStation ! Vote("123", "Billy Bob")
  votingStation ! Vote("444", "Billy Bob")
  votingStation ! Vote("777", "Ray MacNab")

  votingStation ! Vote("777", "Ray MacNab")

  votingStation ! "print"
}

class VotingStation extends PersistentActor with ActorLogging {
  var citizens: Set[String] = Set.empty
  var poll: Map[String, Int] = Map.empty[String, Int]

  def persistenceId: String = "voting-station"

  def receiveCommand: Receive = {
    case vote @ Vote(citizenPid, candidate) =>
      if (citizens contains citizenPid) {
        log warning s"Citizen $citizenPid attempted to vote again (for candidate $candidate)"
      } else {
        persist(vote)(updatePoll)
      }

    case "print" =>
      log info s"Citizens: ${citizens mkString ", "}"
      log info s"Poll: ${poll mkString ", "}"
  }

  def receiveRecover: Receive = {
    case vote: Vote => updatePoll(vote)
  }

  def updatePoll(vote: Vote): Unit = {
    citizens = citizens + vote.citizenPid
    poll = poll.update(vote.candidate, 0)(_ + 1)
  }
}

final case class Vote(citizenPid: String, candidate: String)