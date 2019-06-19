package com.backwards.persistence.ex1

import java.util.Date
import scala.collection.immutable
import scala.concurrent.ExecutionContextExecutor
import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Scenario: We have a business and an accountant which keeps track of our invoices.
  *
  * NOTE - Never call "persist" or "persistAll" from futures as you can have multiple threads executing.
  *
  * NOTE - Be weary of how you shutdown a peristent actor because of the time gap between persisting and the callback.
  * Any incoming commands during the time gap are stashed, but if a PoisonPill is received, then stashed commands can be lost.
  * The best practice is to define your own shutdown message - below we have a Shutdown object.
  */
object PersistentActors extends App {
  val system = ActorSystem("persistent-actors")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(3 seconds)

  val accountant = system.actorOf(Props[Accountant], "accountant")

  for (i <- 1 to 10) {
    accountant ! Invoice("The Sofa Company", new Date, i * 1000)
  }

  accountant ! Invoices((1 to 5).map(i => Invoice("Awesome Chairs", new Date, i * 1000)))

  (accountant ? Shutdown).onComplete { _ =>
    system.terminate().map(_ => sys.exit())
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

    case Invoices(invoices) =>
      /*
      1) Create events (plural)
      2) Persist all the events
      3) Update the actor state when each event is persisted.
      */
      log info s"Received bulk invoices"
      val invoiceIds: immutable.Seq[Int] = latestInvoiceId to (latestInvoiceId + invoices.length)

      val events: immutable.Seq[InvoiceRecorded] = invoices.zip(invoiceIds).map { case (invoice, id) =>
        InvoiceRecorded(id, invoice.recipient, invoice.date, invoice.amount)
      }

      persistAll(events) { event =>
        latestInvoiceId += 1
        totalAmount += event.amount
        log info s"Persisted $event as invoice #${event.id}, giving total amount $totalAmount"
      }

    // Can still act as a normal actor
    case "print" =>
      log info s"Latest invoice id: $latestInvoiceId, total amount: $totalAmount"

    case Shutdown =>
      // Shutdown would have been put into the normal mailbox
      context stop self
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

  /**
    * Called when "persist" method fails - The actor will be "stopped".
    * Best practice to start actor again (after a while) using Backoff supervisor.
    */
  override protected def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    log error s"Failed to persist $event because of $cause"
    super.onPersistFailure(cause, event, seqNr)
  }

  /**
    * Called if Journal fails to persist the event.
    * The actor is "resumed".
    */
  override protected def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit = {
    log error s"Persist rejected for $event because of $cause"
    super.onPersistRejected(cause, event, seqNr)
  }
}

/**
  * Command.
  */
final case class Invoice(recipient: String, date: Date, amount: Int)

/**
  * Command.
  */
final case class Invoices(value: immutable.Seq[Invoice]) extends AnyVal

/**
  * Command.
  */
case object Shutdown

/**
  * Event.
  */
final case class InvoiceRecorded(id: Int, recipient: String, data: Date, amount: Int)