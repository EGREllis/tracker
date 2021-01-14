package net.finance.tracker.util.pattern;

public interface Listener<T> {
    void listen(T item);
}
