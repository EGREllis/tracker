package net.finance.tracker.util;

public interface Listener<T> {
    void listen(T item);
}
