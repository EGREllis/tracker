package net.finance.tracker.util.logging;

import net.finance.tracker.util.pattern.Listener;

public class LoggingListener implements Listener<Exception> {
    @Override
    public void listen(Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace(System.err);
        System.err.flush();
    }
}
