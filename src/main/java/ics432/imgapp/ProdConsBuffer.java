package ics432.imgapp;

import java.util.ArrayDeque;

public class ProdConsBuffer {

    public static final WorkUnit theEnd = new WorkUnit(null, null, null);

    private ArrayDeque<WorkUnit> q;

    private int maxCapacity;


    public ProdConsBuffer(int maxCapacity) {
        this.q = new ArrayDeque<>();
        this.maxCapacity = maxCapacity;
    }

    public synchronized void put(WorkUnit wu) throws InterruptedException {

        // Wait  for the queue to be not full
        while (this.q.size() >= maxCapacity) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw e;
            }
        }

        // Insert the work unit
        this.q.addFirst(wu);

        // Signal that the list is not empty
        this.notifyAll();
    }

    public synchronized WorkUnit get() throws InterruptedException {

        // Wait  for the queue to be not empty
        while (this.q.size() == 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw e;
            }
        }

        // Remove the WorkUnit to return
        WorkUnit toReturn = this.q.removeLast();

        // Signal that the list is not full
        this.notifyAll();

        return toReturn;
    }

    public int size() {
        return this.q.size();
    }
}
