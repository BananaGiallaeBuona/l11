package it.unibo.oop.workers01;

import java.util.List;

/**
 * The sum of all elements in a list of integers.
 */
public interface SumList {

    /**
     * @param list
     *            an arbitrary-sized list of integers
     * @return the sum of its elements
     */
    long sum(List<Integer> list);

}
