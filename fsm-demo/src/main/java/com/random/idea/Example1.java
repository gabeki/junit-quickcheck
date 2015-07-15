package com.random.idea;

import java.io.IOException;

import org.junit.contrib.theories.GraphTheories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.pholser.junit.quickcheck.ForAll;

/**
 * Show the differences between Theories and GraphTheories.
 */
//@RunWith(Theories.class)
@RunWith(GraphTheories.class)
public class Example1 {

    private static Logger logger = LoggerFactory.getLogger(Example1.class);

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
}

