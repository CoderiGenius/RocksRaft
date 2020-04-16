package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Extend array list to add peek/poll first/last element.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Apr-11 11:14:38 AM
 * @param <E>
 */
public class ArrayDeque<E> extends java.util.ArrayList<E> {

    private static final long serialVersionUID = -4929318149975955629L;

    /**
     * Get the first element of list.
     */
    public static <E> E peekFirst(List<E> list) {
        return list.get(0);
    }

    /**
     * Remove the first element from list and return it.
     */
    public static <E> E pollFirst(List<E> list) {
        return list.remove(0);
    }

    /**
     * Get the last element of list.
     */
    public static <E> E peekLast(List<E> list) {
        return list.get(list.size() - 1);
    }

    /**
     * Remove the last element from list and return it.
     */
    public static <E> E pollLast(List<E> list) {
        return list.remove(list.size() - 1);
    }

    /**
     * Get the first element of list.
     */
    public E peekFirst() {
        return peekFirst(this);
    }

    /**
     * Get the last element of list.
     */
    public E peekLast() {
        return peekLast(this);
    }

    /**
     * Remove the first element from list and return it.
     */
    public E pollFirst() {
        return pollFirst(this);
    }

    /**
     * Remove the last element from list and return it.
     */
    public E pollLast() {
        return pollLast(this);
    }

    /**
     * Expose this methods so we not need to create a new subList just to
     * remove a range of elements.
     *
     * Removes from this deque all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the deque by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * @throws IndexOutOfBoundsException if {@code fromIndex} or
     *         {@code toIndex} is out of range
     *         ({@code fromIndex < 0 ||
     *          fromIndex >= size() ||
     *          toIndex > size() ||
     *          toIndex < fromIndex})
     */
    @Override
    public void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
    }
}
