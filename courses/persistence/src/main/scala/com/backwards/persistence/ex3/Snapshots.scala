package com.backwards.persistence.ex3

import scala.collection.mutable
import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.backwards.persistence.ex3.Chat._

object Snapshots extends App {
  val system = ActorSystem("snapshots")

  val chat = system.actorOf(Chat.props("david123", "bob123"), "chat")

  (1 to 50000) foreach { i =>
    chat ! ReceivedMessage(s"Akka Ye Baby $i")
    chat ! SentMessage(s"Oh Ye $i")
  }

  chat ! "print"
}

class Chat(owner: Owner, contact: Contact) extends PersistentActor with ActorLogging {
  val maxMessages = 10

  var commandsWithoutCheckpoint = 0
  var currentMessageId = 0
  val lastMessages = new mutable.Queue[(Originator, Contents)]

  def persistenceId: String = s"$owner-$contact-chat"

  def receiveCommand: Receive = {
    case SentMessage(contents) =>
      persist(SentMessageRecord(currentMessageId, contents)) { event =>
        log info event.toString
        queue(owner, contents)
        currentMessageId += 1
        checkpoint()
      }

    case ReceivedMessage(contents) =>
      persist(ReceivedMessageRecord(currentMessageId, contents)) { event =>
        log info event.toString
        queue(contact, contents)
        currentMessageId += 1
        checkpoint()
      }

    case "print" =>
      log info s"Most recent messages:\n${lastMessages.mkString("\n")}"

    case SaveSnapshotSuccess(metadata) =>
      log info s"Saving snapshot succeeded: $metadata"

    case SaveSnapshotFailure(metadata, throwable) =>
      log warning s"Saving snapshot $metadata failed because of $throwable"
  }

  def receiveRecover: Receive = {
    case r @ SentMessageRecord(id, contents) =>
      log info s"Recovered: $r"
      queue(owner, contents)
      currentMessageId = id

    case r @ ReceivedMessageRecord(id, contents) =>
      log info s"Recovered: $r"
      queue(contact, contents)
      currentMessageId = id

    case SnapshotOffer(metadata, payload) =>
      log info s"Recovered snapshot: $metadata"
      payload.asInstanceOf[mutable.Queue[(Originator, Contents)]].foreach(lastMessages.enqueue(_))
  }

  def queue(originator: Originator, contents: Contents): Unit = {
    if (lastMessages.size >= maxMessages) {
      lastMessages.dequeue()
    }

    lastMessages.enqueue(originator -> contents)
  }

  def checkpoint(): Unit = {
    commandsWithoutCheckpoint += 1

    if (commandsWithoutCheckpoint >= maxMessages) {
      log info s"Saving checkpoint..."
      saveSnapshot(lastMessages) // <--- asynchronous operation and if this is successful a "snapshot success" is fired otherwise a "snapshot failed"
      commandsWithoutCheckpoint = 0
    }
  }
}

object Chat {
  type Originator = String
  type Owner = String
  type Contact = String
  type Contents = String

  def props(owner: Owner, contact: Contact): Props =
    Props(new Chat(owner, contact))
}

/** Commands */

final case class SentMessage(contents: Contents)      // Message TO your contact

final case class ReceivedMessage(contents: Contents)  // Message FROM you contact

/** Events */

final case class SentMessageRecord(id: Int, contents: Contents)

final case class ReceivedMessageRecord(id: Int, contents: Contents)