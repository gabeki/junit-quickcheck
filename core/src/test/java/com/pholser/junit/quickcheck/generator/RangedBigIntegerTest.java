/*
 The MIT License

 Copyright (c) 2010-2013 Paul R. Holser, Jr.

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

package com.pholser.junit.quickcheck.generator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.math.BigInteger.*;
import static java.util.Arrays.*;

import com.pholser.junit.quickcheck.internal.generator.GeneratingUniformRandomValuesForTheoryParameterTest;

import static org.mockito.Mockito.*;

public class RangedBigIntegerTest extends GeneratingUniformRandomValuesForTheoryParameterTest {
    private final BigInteger min = new BigInteger("-12345678123456781234567812345678");
    private final BigInteger max = new BigInteger("987654321987654321");

    @Override protected void primeSourceOfRandomness() {
        int numberOfBits = max.subtract(min).bitLength();
        when(randomForParameterGenerator.nextBigInteger(numberOfBits))
            .thenReturn(new BigInteger("2").pow(numberOfBits).subtract(ONE)).thenReturn(ONE).thenReturn(TEN)
            .thenReturn(ZERO).thenReturn(new BigInteger("234234234234"));
    }

    @Override protected Type parameterType() {
        return BigInteger.class;
    }

    @Override protected int sampleSize() {
        return 4;
    }

    @Override protected List<?> randomValues() {
        return asList(min.add(ONE), min.add(TEN), min.add(ZERO), min.add(new BigInteger("234234234234")));
    }

    @Override protected Map<Class<? extends Annotation>, Annotation> configurations() {
        InRange range = mock(InRange.class);
        when(range.min()).thenReturn(min.toString());
        when(range.max()).thenReturn(max.toString());
        return Collections.<Class<? extends Annotation>, Annotation> singletonMap(InRange.class, range);
    }

    @Override public void verifyInteractionWithRandomness() {
        verify(randomForParameterGenerator, times(5)).nextBigInteger(max.subtract(min).bitLength());
    }
}
