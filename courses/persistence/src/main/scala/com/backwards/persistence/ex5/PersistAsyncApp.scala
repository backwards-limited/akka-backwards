package com.backwards.persistence.ex5

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence.PersistentActor
import com.backwards.persistence.ex5.StreamProcessor.{Command, Event}

object PersistAsyncApp extends App {
  val system = ActorSystem("persist-async")

  val eventAggregator = system.actorOf(Props[EventAggregator], "event-aggregator")
  val streamProcessor = system.actorOf(StreamProcessor props eventAggregator, "stream-processor")

  streamProcessor ! Command("command 1")
  streamProcessor ! Command("command 2")
}

class StreamProcessor(eventAggregator: ActorRef) extends PersistentActor with ActorLogging {
  def persistenceId: String = "stream-processor"

  def receiveCommand: Receive = {
    case Command(contents) =>
      eventAggregator ! s"Processing $contents"

      persistAsync(Event(contents)) { event =>
        eventAggregator ! event
      }

      val processedContents = s"$contents - processed"

      persistAsync(Event(processedContents)) { event =>
        eventAggregator ! event
      }
  }

  def receiveRecover: Receive = {
    case message => log info s"Recovered: $message"
  }
}

object StreamProcessor {
  def props(eventAggregator: ActorRef): Props = Props(new StreamProcessor(eventAggregator))

  final case class Command(contents: String)

  final case class Event(contents: String)
}

class EventAggregator extends Actor with ActorLogging {
  def receive: Receive = {
    case message => log info s"$message"
  }
}