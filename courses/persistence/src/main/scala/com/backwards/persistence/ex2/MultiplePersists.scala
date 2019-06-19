package com.backwards.persistence.ex2

import java.util.Date
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence.PersistentActor

/**
  * Accountant with every invoice will persist 2 events:
  * - a tax record for the fiscal authority
  * - an invoice record for personal logs or some auditing authority
  */
object MultiplePersists extends App {
  val system = ActorSystem("multiple-persists")

  val taxAuthority = system.actorOf(Props[TaxAuthority], "tax-authority")
  val accountant = system.actorOf(Accountant.props("52352-58325", taxAuthority), "accountant")

  accountant ! Invoice("The Sofa Company", new Date, 2000)
  accountant ! Invoice("The Supercar Company", new Date, 20342350)
}

class Accountant(taxId: String, taxAuthority: ActorRef) extends PersistentActor with ActorLogging {
  var latestRecordId = 0
  var latestInvoiceRecordId = 0

  def persistenceId: String = "accountant"

  def receiveCommand: Receive = {
    case Invoice(recipient, date, amount) =>
      persist(TaxRecord(taxId, latestRecordId, date, amount / 3)) { taxRecord =>
        taxAuthority ! taxRecord
        latestRecordId += 1

        persist("I hereby declare this tax record to be true and complete") { declaration =>
          taxAuthority ! declaration
        }
      }

      persist(InvoiceRecord(latestInvoiceRecordId, recipient, date, amount)) { invoiceRecord =>
        taxAuthority ! invoiceRecord
        latestInvoiceRecordId += 1

        persist("I hereby declare this invoice record to be true") { declaration =>
          taxAuthority ! declaration
        }
      }
  }

  def receiveRecover: Receive = {
    case event => log info s"Received: $event"
  }
}

object Accountant {
  def props(taxId: String, taxAuthority: ActorRef): Props = Props(new Accountant(taxId, taxAuthority))
}

/**
  * Ordering of messages will be:
  *
  * - Received: TaxRecord(52352-58325,0,Wed Jun 19 11:22:50 BST 2019,666)
  * - Received: InvoiceRecord(0,The Sofa Company,Wed Jun 19 11:22:50 BST 2019,2000)
  * - Received: I hereby declare this tax record to be true and complete
  * - Received: I hereby declare this invoice record to be true
  *
  * - Received: TaxRecord(52352-58325,1,Wed Jun 19 11:22:50 BST 2019,6780783)
  * - Received: InvoiceRecord(1,The Supercar Company,Wed Jun 19 11:22:50 BST 2019,20342350)
  * - Received: I hereby declare this tax record to be true and complete
  * - Received: I hereby declare this invoice record to be true
  */
class TaxAuthority extends Actor with ActorLogging {
  def receive: Receive = {
    case message => log info s"Received: $message"
  }
}

/** Command */
case class Invoice(recipient: String, date: Date, amount: Int)

/** Event */
case class TaxRecord(taxId: String, recordId: Int, date: Date, totalAmount: Int)

/** Event */
case class InvoiceRecord(invoiceRecordId: Int, recipient: String, date: Date, amount: Int)