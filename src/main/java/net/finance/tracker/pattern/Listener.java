package net.finance.tracker.pattern;

public interface Listener<T> {
    void listen(T item);
}
