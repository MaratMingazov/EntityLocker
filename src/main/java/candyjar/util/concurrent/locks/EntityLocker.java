package candyjar.util.concurrent.locks;

/**
 * EntityLocker is a reusable utility class that provides synchronization mechanism similar to row-level DB locking.
 * The class is supposed to be used by the components that are responsible for managing storage and caching of different type of entities in the application. EntityLocker itself does not deal with the entities, only with the IDs (primary keys) of the entities.
 * Features:
 * 1. EntityLocker supports different types of entity IDs (implemented as Generic Class).
 * 2. EntityLocker allow the caller to specify which entity does it want to work with (using entity ID), and designate the boundaries of the code that should have exclusive access to the entity (called “protected code”).
 * 3. For any given entity, EntityLocker guarantees that at most one thread executes protected code on that entity. If there’s a concurrent request to locks the same entity, the other thread wait until the entity becomes available.
 * 4. EntityLocker allows concurrent execution of protected code on different entities.
 * 5. EntityLocker allows reentrant locking.
 * 6. EntityLocker allows the caller to specify timeout for locking an entity using tryLock(timeout) method.
 * 7. EntityLocker implements protection from deadlocks. You can use tryLock() method for this reason
 * 8. EntityLocker implements global locks. Protected code that executes under a global locks not executes concurrently with any other protected code.
 *
 * developed by Marat Mingazov 05.04.2021
 */

import java.util.HashMap;
import java.util.Map;

public class EntityLocker<T>  {


    private Map<T, Thread> lockMap = new HashMap<>();
    private Map<T, Integer> lockCountMap = new HashMap<>();

    private volatile Thread  globalLockedBy = null;
    private volatile int globalLocksCounter = 0;

    /**
     * Acquires the locks.
     * Acquires the locks if it is not held by another thread on some EntityID and returns immediately, setting the locks hold count to one for this EntityID.
     * If the current thread already holds the locks on some EntityID then the hold count is incremented by one and the method returns immediately.
     * If the locks is held by another thread on some EntityID then the current thread becomes disabled for thread scheduling purposes and lies dormant until the locks has been acquired, at which time the locks hold count is set to one.
     * @param key the EntityID to be locked (primary ID)
     * @throws InterruptedException
     */
    public final synchronized void lock(final T key) throws InterruptedException {
        Thread callingThread = Thread.currentThread();
        while ((lockCountMap.getOrDefault(key, 0) > 0 && lockMap.get(key) != callingThread) || globalLocksCounter > 0) {
            wait();
        }
        lockMap.put(key, callingThread);
        lockCountMap.merge(key, 1, Integer::sum);
    }

    /**
     * Acquires the locks only if it is not held by another thread on some EntityID at the time of invocation.
     * If the current thread already holds this locks on some EntityID then the hold count is incremented by one and the method returns true.
     * If the locks is held by another thread on some EntityID then this method will return immediately with the value false.
     * @param key the EntityID to be locked (primary ID)
     * @return true if the locks was free and was acquired by the current thread on some EntityID, or the locks was already held by the current thread on some EntityID; and false otherwise
     * @throws InterruptedException
     */
    public final synchronized boolean tryLock(final T key) throws InterruptedException {
        Thread callingThread = Thread.currentThread();
        if ((lockCountMap.getOrDefault(key, 0) > 0 && lockMap.get(key) != callingThread) || globalLocksCounter > 0) {
            return false;
        }
        lockMap.put(key, callingThread);
        lockCountMap.merge(key, 1, Integer::sum);
        return true;
    }

    /**
     * Acquires the locks if it is not held by another thread within the given waiting time on some EntityID and the current thread has not been interrupted.
     * If the current thread already holds this locks on some EntityID then the hold count is incremented by one and the method returns true.
     * If the locks is held by another thread on some EntityID then the current thread becomes disabled for thread scheduling purposes and lies dormant until one of three things happens:
     * 1 - The locks is acquired by the current thread; or
     * 2 - Some other thread interrupts the current thread; or
     * 3 - The specified waiting time elapses
     * If the locks is acquired then the value true is returned and the locks hold count is set to one.
     * @param key - the EntityID to be locked (primary ID)
     * @param timeout -  the time to wait for the locks
     * @return true if the locks was free and was acquired by the current thread on some EntityID, or the locks was already held by the current thread; and false if the waiting time elapsed before the locks could be acquired
     * @throws InterruptedException
     */
    public final synchronized boolean tryLock(final T key, final long timeout) throws InterruptedException {
        Thread callingThread = Thread.currentThread();
        if ((lockCountMap.getOrDefault(key, 0) > 0 && lockMap.get(key) != callingThread) || globalLocksCounter > 0) {
            wait(timeout);
            if ((lockCountMap.getOrDefault(key, 0) > 0 && lockMap.get(key) != callingThread) || globalLocksCounter > 0) {
                return false;
            }
        }
        lockMap.put(key, callingThread);
        lockCountMap.merge(key, 1, Integer::sum);
        return true;
    }


    /**
     * Attempts to release this locks on some EntityID.
     * If the current thread is the holder of this locks on some EntityID then the hold count is decremented. If the hold count is now zero then the locks is released on some EntityID.
     * @param key the EntityID to be unlocked (primary ID)
     */
    public final synchronized void unlock(final T key) {
        if (Thread.currentThread() == lockMap.get(key)) {
            int lockCount = lockCountMap.get(key);
            lockCountMap.put(key, --lockCount);
            if (lockCount == 0) {
                lockMap.put(key, null);
                notifyAll();
            }
        }
    }

    /**
     * Acquires the global locks.
     * Global locks means that protected code that executes under this locks not executes concurrently with any other protected code.
     * Acquires the global locks if it is not held by another thread and returns immediately, setting the global locks hold count to one.
     * If the current thread already holds the global locks then the hold count is incremented by one and the method returns immediately.
     * If the locks is held by another thread then the current thread becomes disabled for thread scheduling purposes and lies dormant until the locks has been acquired, at which time the locks hold count is set to one.
     * @throws InterruptedException
     */
    public final synchronized void globalLock() throws InterruptedException {
        Thread callingThread = Thread.currentThread();
        while ((globalLocksCounter > 0 && globalLockedBy != callingThread) || isLocalBlocked()) {
            wait();
        }
        globalLocksCounter++;
        globalLockedBy = callingThread;
    }

    private boolean isLocalBlocked() {
        for (Integer value : lockCountMap.values()) {
            if (value > 0) {
                return true;
            }
        }
        return false;
    }


    /**
     * Attempts to release this global locks.
     * If the current thread is the holder of this global locks then the hold count is decremented. If the hold count is now zero then the global locks is released.
     */
    public final synchronized void globalUnlock() {
        if (Thread.currentThread() == globalLockedBy) {
            globalLocksCounter--;
            if (globalLocksCounter == 0) {
                notifyAll();
            }
        }
    }

}