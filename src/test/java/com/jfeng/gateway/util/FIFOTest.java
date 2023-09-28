package com.jfeng.gateway.util;

import junit.framework.TestCase;

public class FIFOTest extends TestCase {

    public void testAdd() {
        FIFO<Integer> fifo  = new FIFO<>(3);
        fifo.add(1);
        fifo.add(2);
        fifo.add(3);
        fifo.add(4);
        fifo.print();

        fifo.add(5);
        fifo.add(7);
        fifo.add(8);
        fifo.print();
    }

    public void testRemove() {
    }
}