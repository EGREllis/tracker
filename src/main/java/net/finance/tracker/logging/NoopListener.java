package net.finance.tracker.logging;

import net.finance.tracker.pattern.Listener;

public class NoopListener implements Listener<Exception> {
    @Override
    public void listen(Exception item) {
        // When running in silent mode we deliberately swallow exceptions.
    }
}
