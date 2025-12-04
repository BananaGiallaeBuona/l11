package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

/**
 * our goal is to:
 * 1)obtain the number of cells in the matrix.
 * 2)divide it for somthing .
 */
public final class SumMatrixImplementation implements SumMatrix {
    private final int nthread;

    SumMatrixImplementation(final int nthread) {
        this.nthread = nthread;
    }

    @Override
    public double sum(final double[][] matrix) {
        return sum(transformInList(matrix));
    }

    /**
     * @param matrixList the matrix transformed in list
     * 
     * @return a double that represent the sum of every element inside a cell
     */
    public double sum(final List<Double> matrixList) {
        final int size = matrixList.size() % nthread + matrixList.size() / this.nthread;
        double sum = 0;
        /*
         * Build a list of workers
         */
        final List<Worker> workers = new ArrayList<>(nthread);
        for (int start = 0; start < matrixList.size(); start += size) {
            workers.add(new Worker(matrixList, start, size));
        }
        /*
         * Start them
         */
        for (final Worker w: workers) {
            w.start();
        }
        /*
         * Wait for every one of them to finish. This operation is _way_ better done by
         * using barriers and latches, and the whole operation would be better done with
         * futures.
         */
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        /*
         * Return the sum
         */
        return sum;
    }

    private List<Double> transformInList(final double[][] matrix) {
        final List<Double> list = new ArrayList<>();
        for (final double[] row : matrix) {
            for (final double val : row) {
                list.add(val);
            }
        }
        return list;
    }

    private static class Worker extends Thread {
        private final List<Double> list;
        private final int startpos;
        private final int nelem;
        private double res;

        /**
         * Build a new worker.
         *
         * @param list
         *            the list to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final List<Double> list, final int startpos, final int nelem) {
            super();
            this.list = list;
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        public synchronized void run() {
            // Println used to show the working ranges for debugging purposes
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1)); // NOPMD
            for (int i = startpos; i < list.size() && i < startpos + nelem; i++) {
                this.res += this.list.get(i);
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         *
         * @return the sum of every element in the array
         */
        public synchronized double getResult() {
            return this.res;
        }

    }
}
