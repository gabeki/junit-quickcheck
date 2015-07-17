package com.random.idea;

import static org.junit.contrib.theories.GraphTheories.TheoryEdge;
import static org.junit.contrib.theories.GraphTheories.TheoryVertex;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.pholser.junit.quickcheck.ForAll;
import org.junit.contrib.theories.GraphTheories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Give weights to the graph.
 */
@RunWith(GraphTheories.class)
@GraphTheories.ForAllTheories(sampleTimeUnit = TimeUnit.SECONDS, min = 2, max = 5)
public class Example3 {

    private static Logger logger = LoggerFactory.getLogger(Example3.class);

    @Theory
    @TheoryVertex(isStart = true, loopToSelf = false)
    public void f1() throws IOException {
        logger.debug("#f1()");
    }

    @Theory
    @TheoryVertex(connectTo = {
            @TheoryEdge(name = "f3", weight = 0.8),
            @TheoryEdge(name = "f4", weight = 0.1),
            @TheoryEdge(name = "f1", weight = 0.0)
    })
    public void f2(@ForAll(sampleSize=1) String s) throws Exception {
        logger.debug("#f2(" + s + ")");
    }

    @Theory
    @TheoryVertex(connectTo = {
            @TheoryEdge(name = "f1", weight = 0)
    })
    public void f3(@ForAll(sampleSize=1) int i) throws IOException {
        logger.debug("#f3(" + i + ")");
    }

    @Theory
    public void f4(@ForAll(sampleSize=1) int i) throws IOException {
        logger.debug("#f4(" + i + ")");
    }
}
