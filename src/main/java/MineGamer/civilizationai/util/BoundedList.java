package MineGamer.civilizationai.util;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * A FIFO list capped at a fixed maximum size. When a new element would push
 * the list past its cap, the oldest element is evicted first.
 * <p>
 * Every villager memory category that could otherwise grow without bound
 * over a long-lived world (trades, danger events, past raids, weather
 * history, travel routes) is backed by one of these rather than a plain
 * {@code ArrayList}. This is a deliberate, load-bearing part of the "no
 * memory leaks" performance goal — bounding happens at the data-structure
 * level, not by remembering to prune call sites later.
 *
 * @param <T> element type
 */
public final class BoundedList<T> implements Iterable<T> {

    private final int capacity;
    private final Deque<T> elements;

    public BoundedList(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive, was " + capacity);
        }
        this.capacity = capacity;
        this.elements = new ArrayDeque<>(Math.min(capacity, 64));
    }

    /**
     * Appends an element, evicting the oldest entry first if the list is
     * already at capacity.
     */
    public void add(T element) {
        if (elements.size() >= capacity) {
            elements.pollFirst();
        }
        elements.addLast(element);
    }

    public int size() {
        return elements.size();
    }

    public int capacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public List<T> asList() {
        return Collections.unmodifiableList(List.copyOf(elements));
    }

    @Override
    public Iterator<T> iterator() {
        return asList().iterator();
    }
}
