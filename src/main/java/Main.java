import candyjar.util.concurrent.locks.EntityLocker;

public class Main {
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
}
