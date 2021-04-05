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
