package com.random.idea;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.contrib.theories.GraphTheories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.pholser.junit.quickcheck.ForAll;

/**
 * If sampleTime is not set, only 1 test will be generated.  Otherwise
 * GraphTheories generates as many tests as possible in a given time.
 */
@RunWith(GraphTheories.class)
@GraphTheories.ForAllTheories(sampleTime = 10, sampleTimeUnit = TimeUnit.SECONDS, min = 2, max = 5)
public class Example2 {

    private static Logger logger = LoggerFactory.getLogger(Example2.class);

    @BeforeClass
    public static void beforeAllTheories() {
        logger.debug("#beforeAllTheories()");
    }

    @Theory
    public void f1() throws IOException {
        logger.debug("#f1()");
    }

    @Theory
    public void f2(@ForAll(sampleSize=1) String s) throws Exception {
        logger.debug("#f2(" + s + ")");
    }

    @Theory
    public void f3(@ForAll(sampleSize=1) int i) throws IOException {
        logger.debug("#f3(" + i + ")");
    }

    @Theory
    public void f4(@ForAll(sampleSize=1) int i) throws IOException {
        logger.debug("#f4(" + i + ")");
        fail();
    }
}

