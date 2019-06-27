# Recovery

Take a look at [RecoveryApp.scala](../src/main/scala/ex4/RecoveryApp.scala). The first time it is run, 1000 messages are persisted:

```bash
...
... [akka://recovery/user/recovery-actor] Persisted Event(Command 998)
... [akka://recovery/user/recovery-actor] Persisted Event(Command 999)
... [akka://recovery/user/recovery-actor] Persisted Event(Command 1000)
```

If we run again, the original 1000 are first recovered. However, at the same time we again tried to persist another 1000 messages. **But** these new messages cannot be handled until recovery is complete, and so these next 1000 messages are **stashed** before being persisted:

```bash
...
... [akka://recovery/user/recovery-actor] Recovered event with contents: Command 998
... [akka://recovery/user/recovery-actor] Recovered event with contents: Command 999
... [akka://recovery/user/recovery-actor] Recovered event with contents: Command 1000

... [akka://recovery/user/recovery-actor] Persisted Event(Command 1)
... [akka://recovery/user/recovery-actor] Persisted Event(Command 2)
... [akka://recovery/user/recovery-actor] Persisted Event(Command 3)
...
```

So remember: **All commands sent during recovery are stashed**.

## What Happens When an Exception is Thrown During Recovery?

The **onRecoveryFailure** method is initiated with the **Throwable** i.e. the exception that occurred and **possibly** the event that was being recovered.

Let's say an exception occurs when recovering message 314:

```bash
...
... [akka://recovery/user/recovery-actor] Recovered event with contents: Command 312
... [akka://recovery/user/recovery-actor] Recovered event with contents: Command 313
[ERROR] [akka://recovery/user/recovery-actor] I failed at recovery: event = Some(Event(Command 314)), cause = I can't handle 314
[ERROR] [akka://recovery/user/recovery-actor] Exception in receiveRecover when replaying event type [com.backwards.persistence.ex4.Event] with sequence number [314] for persistenceId [recovery-actor].
java.lang.Exception: I can't handle 314
	at com.backwards.persistence.ex4.RecoveryActor$$anonfun$receiveRecover$1.applyOrElse(Recovery.scala:29)
...	
```

After the exception is handled the actor is **stopped**. But what about those stashed commands?

```bash
[akka://recovery/user/recovery-actor] Message [com.backwards.persistence.ex4.Command] without sender to Actor[akka://recovery/user/recovery-actor#-262855277] was not delivered. [1] dead letters encountered. If this is not an expected behavior, then [Actor[akka://recovery/user/recovery-actor#-262855277]] may have terminated unexpectedly, This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.

[akka://recovery/user/recovery-actor] Message [com.backwards.persistence.ex4.Command] without sender to Actor[akka://recovery/user/recovery-actor#-262855277] was not delivered. [2] dead letters encountered. If this is not an expected behavior, then [Actor[akka://recovery/user/recovery-actor#-262855277]] may have terminated unexpectedly, This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
...
```

Well, they cannot be handled / persisted.

So, **failure during recovery**: **onRecoveryFailure** is called and the actor is **stopped**.

We can customise the recovery by overriding the **recovery** method. However, this is mainly useful for **debugging** recovery issues - so **do not** persist more events after a customised recovery, because the actual recovery will be incomplete.

## Recovery Status

Recovery status or knowing when you're done recovering. As an example, we can utilise method **recoveryFinished**. However, this method is pretty useless - in the example application it is certainly completely pointless.

What is useful, is to receive a signal when recovery is complete, and that is **RecoveryCompleted**.