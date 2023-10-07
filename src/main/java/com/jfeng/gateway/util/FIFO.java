package com.jfeng.gateway.util;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;

@Getter
public class FIFO<T> {
    int capacity = 10;
    int offset = 0;
    LinkedList<T> data;

    public FIFO(int capacity) {
        this.capacity = capacity;
        this.data = new LinkedList<>();
    }

    public void add(T value) {
        if (offset == capacity) {
            remove();
        }

        data.addLast(value);
        offset++;
    }

    public void remove() {
        data.removeFirst();
        if (offset > 0) {
            offset--;
        }
    }

    public void clear() {
        data.clear();
        offset = 0;
    }

    public void print() {
        System.out.println(Arrays.toString(data.toArray()));
    }
}
