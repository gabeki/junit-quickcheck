package org.junit.contrib.theories.util;

import java.util.Random;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author Gabriel Ki <gabeki@apple.com>
 */
public class DiscreteChooser<T> {
    private ConcurrentNavigableMap<Double, T> map;
    private Random random;
    private double totalWeight = 0D;

    public DiscreteChooser() {
        random = new Random();
        map = new ConcurrentSkipListMap<Double, T>();
    }

    public DiscreteChooser add(T t, double weight) {
        totalWeight += weight;
        map.put(totalWeight, t);
        return this;
    }

    public T next() {
        return map.ceilingEntry(random.nextDouble() * totalWeight).getValue();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}

