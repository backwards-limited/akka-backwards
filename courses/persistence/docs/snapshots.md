# Snapshots

**The problem:** Long lived entities take a long time to recover.

**The solution:** Save checkpoints.

Snapshot recovery process - let's say the following are persisted:

- event 1
- event 2
- event 3
- snapshot 1
- event 4
- snapshot 2
- event 5
- event 6

During the recovery, only the last snapshot and any subsequent events after said snapshot are recovered, so we can skip potentially millions of events making recovery much faster when compared to not using snapshotting.

**Pattern:**

- After each persist, maybe save a snapshot (logic is up to you).
- If you save a snapshot, handle the **SnapshotOffer** message in **receiveRecover**.
- (Optional but best practice) handle **SaveSnapshotSuccess** and **SaveSnapshotFailure** in **receiveCommand**.