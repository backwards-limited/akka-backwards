# Persist Async

- Relaxing event ordering guarantees.
- Giving higher throughput.

Take a look at [PersistAsyncApp.scala](../src/main/scala/com/backwards/persistence/ex5/PersistAsyncApp.scala), when **case Command** in **receiveCommand** has the following code:

```scala
def receiveCommand: Receive = {
  case Command(contents) =>
    eventAggregator ! s"Processing $contents"

    persist(Event(contents)) /* Time gap */ { event =>
      eventAggregator ! event
    }

    val processedContents = s"$contents - processed"

    persist(Event(processedContents)) /* Time gap */ { event =>
      eventAggregator ! event
    }
}
```

the output is:

```bash
[akka://persist-async/user/stream-processor] Recovered: RecoveryCompleted
[akka://persist-async/user/event-aggregator] Processing command 1
[akka://persist-async/user/event-aggregator] Event(command 1)
[akka://persist-async/user/event-aggregator] Event(command 1 - processed)
[akka://persist-async/user/event-aggregator] Processing command 2
[akka://persist-async/user/event-aggregator] Event(command 2)
[akka://persist-async/user/event-aggregator] Event(command 2 - processed)
```

Now let's change **persist** to **persistAsync**:

```scala
def receiveCommand: Receive = {
  case Command(contents) =>
    eventAggregator ! s"Processing $contents"

    persistAsync(Event(contents)) /*             Time gap              */ { event =>
      eventAggregator ! event
    }

    val processedContents = s"$contents - processed"

    persistAsync(Event(processedContents)) /*             Time gap              */ { event =>
      eventAggregator ! event
    }
}
```

giving different output:

```bash
[akka://persist-async/user/stream-processor] Recovered: RecoveryCompleted
[akka://persist-async/user/event-aggregator] Processing command 1
[akka://persist-async/user/event-aggregator] Processing command 2
[akka://persist-async/user/event-aggregator] Event(command 1)
[akka://persist-async/user/event-aggregator] Event(command 1 - processed)
[akka://persist-async/user/event-aggregator] Event(command 2)
[akka://persist-async/user/event-aggregator] Event(command 2 - processed)
```

So how do **persist** and **persistAsync** work differently? Well even though they actually both execute asynchronously, with regards to **persist**, any command coming into **receiveCommand** during the **time gap** are stashed, to keep all messages **in order**, whereas stashing is not done for **persistAsync** so messages get out of order. And why would you tolerate out of order messages? For greater performance. However, messages handled by **persistAsync** itself are kept in order.