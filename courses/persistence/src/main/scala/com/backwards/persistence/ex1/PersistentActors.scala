package com.backwards.persistence.ex1

import java.util.Date
import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

/**
  * Scenario: We have a business and an accountant which keeps track of our invoices.
  */
object PersistentActors extends App {
  val system = ActorSystem("PersistentActors")

  val accountant = system.actorOf(Props[Accountant], "accountant")

  for (i <- 1 to 10) {
    accountant ! Invoice("The Sofa Company", new Date, i * 1000)
  }
}

class Accountant extends PersistentActor with ActorLogging {
  var latestInvoiceId: Int = 0
  var totalAmount: Int = 0

  def persistenceId: String = "accountant"

  /**
    * The normal "receive" method.
    */
  def receiveCommand: Receive = {
    case Invoice(recipient, date, amount) =>
      /*
      Upon receiving a command:
      1) Create an EVENT to persist into the datastore.
      2) Persist the event and pass in a callback that will be triggered once the event has been written.
      3) Update this actor's state when the event has been persisted.
      */
      log info s"Received invoice for amount: $amount"

      persist(InvoiceRecorded(latestInvoiceId, recipient, date, amount)) /* Time gap: However, other messages received during this time gap are STASHED */ { event =>
        // Safe to access mutable state here.
        // Normally accessing state in an asynchronous callback is unsafe.
        // However, Akka Persistence guarantees that during this callback, only the running thread has access to the internal state, so there can be no race condition.
        // As a result, this is the one callback we could call: sender() ! "something" i.e. we can correctly identify the sender of the command without doing the usual val client = sender()
        // This is because Akka Persistence is also using actor sequential messaging behing the scenes.
        latestInvoiceId += 1
        totalAmount += event.amount
        log info s"Persisted $event as invoice #${event.id}, giving total amount $totalAmount"
      }

    // Can still act as a normal actor
    case "print" =>
      log info s"Latest invoice id: $latestInvoiceId, total amount: $totalAmount"
  }

  /**
    * Handler that will be called on recovery.
    * Best practice to follow the logic in the persist steps of "receiveCommand"
    */
  def receiveRecover: Receive = {
    case InvoiceRecorded(id, _, _, amount) =>
      latestInvoiceId = id
      totalAmount += amount
      log info s"Recovered invoice #$id for amount $amount, and total amount: $totalAmount"
  }
}

/**
  * Command.
  */
final case class Invoice(recipient: String, data: Date, amount: Int)

/**
  * Event.
  */
final case class InvoiceRecorded(id: Int, recipient: String, data: Date, amount: Int)