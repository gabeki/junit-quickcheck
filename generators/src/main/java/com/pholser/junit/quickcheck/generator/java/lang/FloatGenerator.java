/*
 The MIT License

 Copyright (c) 2010-2014 Paul R. Holser, Jr.

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.pholser.junit.quickcheck.generator.java.lang;

import static java.util.Arrays.*;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

/**
 * Produces values for theory parameters of type {@code float} or {@link Float}.
 */
public class FloatGenerator extends Generator<Float> {
    private float min = 0F;
    private float max = 1F;

    @SuppressWarnings("unchecked") public FloatGenerator() {
        super(asList(float.class, Float.class));
    }

    /**
     * Tells this generator to produce values within a specified {@linkplain InRange#min()}  minimum}
     * (inclusive) and/or {@linkplain InRange#max()}  maximum} (exclusive), with uniform distribution.
     *
     * @param range annotation that gives the range's constraints
     */
    public void configure(InRange range) {
        if (!range.min().isEmpty())
            min = Float.parseFloat(range.min());
        if (!range.max().isEmpty())
            max = Float.parseFloat(range.max());
    }

    @Override public Float generate(SourceOfRandomness random, GenerationStatus status) {
        return random.nextFloat(min, max);
    }
}
