package de.troido.bledemo.util;

public final class Threads {

    private Threads() {}

    /**
     * {@link Thread#join()} with caught {@link InterruptedException} which
     * prints the stack trace.
     * @see Thread#join()
     */
    public static void interruptibleJoin(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@link Thread#sleep(long)} with caught {@link InterruptedException}
     * which prints the stack trace.
     * @see Thread#sleep(long)
     */
    public static void interruptibleSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void interruptibleSleep(long millis, int nanos) {
        try {
            Thread.sleep(millis, nanos);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
