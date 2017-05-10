package de.troido.bledemo.util;

public final class Threads {

    private Threads() {}

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
}
