package com.random.idea;

import static org.junit.contrib.theories.GraphTheories.TheoryEdge;
import static org.junit.contrib.theories.GraphTheories.TheoryVertex;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.pholser.junit.quickcheck.ForAll;
import org.junit.contrib.theories.GraphTheories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

/**
 * Give weights to the graph.
 */
@RunWith(GraphTheories.class)
@GraphTheories.ForAllTheories(sampleTime = 10, sampleTimeUnit = TimeUnit.SECONDS, min = 2, max = 5)
public class Example3 {

    /**
     * f1 goes to all other methods except itself.   It is the only method allowed to start.
     *
     * @throws IOException
     */
    @Theory
    @TheoryVertex(isStart = true, loopToSelf = false)
    public void f1() throws IOException {
    }

    /**
     * f2 goes to all methods including itself except f1.
     *
     * @param s
     * @throws Exception
     */
    @Theory
    @TheoryVertex(connectTo = {
            @TheoryEdge(name = "f3", weight = 0.2),
            @TheoryEdge(name = "f4", weight = 0.8),
            @TheoryEdge(name = "f1", weight = 0.0)
    })
    public void f2(@ForAll(sampleSize=1) String s) throws Exception {
    }

    /**
     * f3 only goes to f1 and itself. But the weight to f1 is 0, so basically it only goes to self.
     *
     * @param i
     * @throws IOException
     */
    @Theory
    @TheoryVertex(connectTo = {
            @TheoryEdge(name = "f1", weight = 0)
    })
    public void f3(@ForAll(sampleSize=1) int i) throws IOException {
    }

    /**
     * f4 goes to all methods including itself.
     *
     * @param i
     * @param s
     * @throws IOException
     */
    @Theory
    public void f4(@ForAll(sampleSize=1) int i, @ForAll(sampleSize = 1) String s) throws IOException {
    }
}

