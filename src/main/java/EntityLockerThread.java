import candyjar.util.concurrent.locks.EntityLocker;

public class EntityLockerThread<T> extends Thread {

    private EntityLocker<T> locker;
    private T key;
    private final int timeout = 5000;

    public EntityLockerThread(final EntityLocker<T> entityLocker, final T entityKey) {
        locker = entityLocker;
        key = entityKey;
    }

    public final void run() {
        executeLock();
        executeTryLock();
        executeTryLock(3000);
        executeGlobalLock();
    }

    public final void executeLock() {
        try {
            locker.lock(key);
            System.err.println(Thread.currentThread().getName() + " started lock() protected code on entity " + key);
            Thread.sleep(timeout);
            // execute protected code here
        } catch (InterruptedException e) {
            currentThread().interrupt();
        } finally {
            System.err.println(Thread.currentThread().getName() + " finished lock() protected code on entity " + key);
            locker.unlock(key);
        }
    }

    public final void executeTryLock() {
        try {
            if (locker.tryLock(key)) {
                System.err.println(Thread.currentThread().getName() + " started tryLock() protected code on entity " + key);
                Thread.sleep(timeout);
                // execute protected code here
            } else {
                System.err.println(Thread.currentThread().getName() + " refused tryLock() protected code on entity " + key);
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        } finally {
            System.err.println(Thread.currentThread().getName() + " finished tryLock() protected code on entity " + key);
            locker.unlock(key);
        }
    }

    public final void executeTryLock(final long timeout) {
        try {
            if (locker.tryLock(key, timeout)) {
                System.err.println(Thread.currentThread().getName() + " started protected code on entity " + key);
                Thread.sleep(this.timeout);
                // execute protected code here
            } else {
                System.err.println(Thread.currentThread().getName() + " refused protected code on entity " + key);
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        } finally {
            System.err.println(Thread.currentThread().getName() + " finished protected code on entity " + key);
            locker.unlock(key);
        }
    }

    public final void executeGlobalLock() {
        try {
            locker.globalLock();
            System.err.println(Thread.currentThread().getName() + " started global protected code");
            Thread.sleep(timeout);
            // execute protected code here
        } catch (InterruptedException e) {
            currentThread().interrupt();
        } finally {
            System.err.println(Thread.currentThread().getName() + " finished global protected code");
            locker.globalUnlock();
        }
    }
}