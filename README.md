# EntityLocker
EntityLocker is a reusable utility class that provides synchronization mechanism similar to row-level DB locking.

The class is supposed to be used by the components that are responsible for managing storage and caching of different type of entities in the application. EntityLocker itself does not deal with the entities, only with the IDs (primary keys) of the entities.

Features:
1. EntityLocker supports different types of entity IDs (implemented as Generic Class).
```java
import candyjar.util.concurrent.locks.EntityLocker;

public static void main(final String[] args) {
  EntityLocker<Integer> integerLocker = new EntityLocker<>(); // Integer ID
  EntityLocker<String> stringLocker = new EntityLocker<>();   // String ID
}
```
2. EntityLocker allow the caller to specify which entity does it want to work with (using entity ID), and designate the boundaries of the code that should have exclusive access to the entity (called “protected code”).
```java
import candyjar.util.concurrent.locks.EntityLocker;

public static void main(final String[] args) {
  EntityLocker<Integer> integerLocker = new EntityLocker<>();
  final int entityId = 8;

  try {
    integerLocker.lock(entityId);
    // execute protected code here
  } catch (InterruptedException e) {
    Thread.currentThread().interrupt();
  } finally {
    integerLocker.unlock(entityId);
  }
}
```

3. For any given entity, EntityLocker guarantees that at most one thread executes protected code on that entity. If there’s a concurrent request to locks the same entity, the other thread wait until the entity becomes available.
```java
import candyjar.util.concurrent.locks.EntityLocker;

public static void main(final String[] args) {
  EntityLocker<Integer> integerLocker = new EntityLocker<>();
  final int entityId = 8;

  new EntityLockerThread<>(integerLocker, entityId).start();
  new EntityLockerThread<>(integerLocker, entityId).start();
}
// Output:
// Thread-0 started protected code on entity 8
// Thread-0 finished protected code on entity 8
// Thread-1 started protected code on entity 8
// Thread-1 finished protected code on entity 8
```

4. EntityLocker allows concurrent execution of protected code on different entities.
```java
import candyjar.util.concurrent.locks.EntityLocker;

public static void main(final String[] args) {
  EntityLocker<Integer> integerLocker = new EntityLocker<>();
  EntityLocker<String> stringLocker = new EntityLocker<>();

  new EntityLockerThread<>(integerLocker, 1).start();
  new EntityLockerThread<>(integerLocker, 1).start();
  new EntityLockerThread<>(integerLocker, 2).start();
  new EntityLockerThread<>(integerLocker, 2).start();
  new EntityLockerThread<>(stringLocker, "a").start();
  new EntityLockerThread<>(stringLocker, "a").start();
}
// Output:
// Thread-3 started protected code on entity 2
// Thread-4 started protected code on entity a
// Thread-0 started protected code on entity 1
// Thread-4 finished protected code on entity a
// Thread-0 finished protected code on entity 1
// Thread-3 finished protected code on entity 2
// Thread-1 started protected code on entity 1
// Thread-2 started protected code on entity 2
// Thread-5 started protected code on entity a
// Thread-1 finished protected code on entity 1
// Thread-2 finished protected code on entity 2
// Thread-5 finished protected code on entity a
```

5. EntityLocker allows reentrant locking.
```java
import candyjar.util.concurrent.locks.EntityLocker;

public static void main(final String[] args) {
  EntityLocker<Integer> integerLocker = new EntityLocker<>();
  final int entityId = 5;

  try {
    integerLocker.lock(entityId);
    integerLocker.lock(entityId);
    // execute protected code here
  } catch (InterruptedException e) {
    Thread.currentThread().interrupt();
  } finally {
    integerLocker.unlock(entityId);
    integerLocker.unlock(entityId);
  }
}
```

6. EntityLocker allows the caller to specify timeout for locking.
```java
import candyjar.util.concurrent.locks.EntityLocker;

public static void main(final String[] args) {
  EntityLocker<Integer> integerLocker = new EntityLocker<>();
  final int entityId = 5;
  final long timeout = 1000;

  try {
    if (integerLocker.tryLock(entityId, timeout)) {
      // execute protected code here
    } else {
      // locking refused code
    }
  } catch (InterruptedException e) {
    Thread.currentThread().interrupt();
  } finally {
    integerLocker.unlock(entityId);
  }
}
```

7. EntityLocker implements protection from deadlocks.
```java
import candyjar.util.concurrent.locks.EntityLocker;

public static void main(final String[] args) {
  EntityLocker<Integer> integerLocker = new EntityLocker<>();
  final int entityId = 5;

  try {
    if (integerLocker.tryLock(entityId)) {
      // execute protected code here
    } else {
      // locking refused code
    }
  } catch (InterruptedException e) {
    Thread.currentThread().interrupt();
  } finally {
    integerLocker.unlock(entityId);
  }
}
```

8. EntityLocker implements global locks. Protected code that executes under a global locks not executes concurrently with any other protected code.
```java
import candyjar.util.concurrent.locks.EntityLocker;

public static void main(final String[] args) {
  EntityLocker<Integer> integerLocker = new EntityLocker<>();

  new EntityLockerThread<>(integerLocker, 2).start();
  new EntityLockerThread<>(integerLocker, 3).start();

  try {
    integerLocker.globalLock();
    System.err.println("global lock");
  } catch (InterruptedException e) {
    Thread.currentThread().interrupt();
  } finally {
    System.err.println("global unlock");
    integerLocker.globalUnlock();
  }

}
// Output:
// global lock
// global unlock
// Thread-0 started protected code on entity 2
// Thread-1 started protected code on entity 3
// Thread-0 finished protected code on entity 2
// Thread-1 finished protected code on entity 3
```
