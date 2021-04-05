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
