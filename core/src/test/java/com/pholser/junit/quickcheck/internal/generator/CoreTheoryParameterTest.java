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

package com.pholser.junit.quickcheck.internal.generator;

import com.pholser.junit.quickcheck.ForAll;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.internal.GeometricDistribution;
import com.pholser.junit.quickcheck.internal.ParameterContext;
import com.pholser.junit.quickcheck.internal.Reflection;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.pholser.junit.quickcheck.test.generator.TestGeneratorSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.theories.PotentialAssignment;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;

import static com.pholser.junit.quickcheck.Objects.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public abstract class CoreTheoryParameterTest {
    @Rule public final MockitoRule mockito = MockitoJUnit.rule();

    @Mock protected SourceOfRandomness randomForParameterGenerator;
    @Mock protected SourceOfRandomness randomForGeneratorRepo;
    @Mock protected GeometricDistribution distro;
    @Mock protected Logger log;
    protected Iterable<Generator<?>> source;
    protected GeneratorRepository repository;
    @Mock private ForAll quantifier;
    @Mock private From explicitGenerators;
    private List<PotentialAssignment> theoryParameters;

    @Before public final void beforeEach() throws Exception {
        source = generatorSource();
        primeSourceOfRandomness();
        primeSeed();
        primeSampleSize();

        repository = new GeneratorRepository(randomForGeneratorRepo).register(source);
        Iterable<Generator<?>> auxiliarySource = auxiliaryGeneratorSource();
        if (auxiliarySource != null)
            repository.register(auxiliarySource);

        RandomTheoryParameterGenerator generator =
            new RandomTheoryParameterGenerator(randomForParameterGenerator, repository, distro, log);

        AnnotatedType type = annotatedType();
        ParameterContext context =
            new ParameterContext("arg", type, type.toString())
                .annotate(annotatedElement());
        context.addQuantifier(quantifier);

        theoryParameters = generator.generate(context);
    }

    protected Iterable<Generator<?>> generatorSource() {
        return new TestGeneratorSource();
    }

    private void primeSampleSize() {
        when(quantifier.sampleSize()).thenReturn(sampleSize());
    }

    private void primeSeed() {
        long seed = seed();
        when(quantifier.seed()).thenReturn(seed);
        when(randomForParameterGenerator.seed()).thenReturn(seed);
    }

    protected long seed() {
        return (long) Reflection.defaultValueOf(ForAll.class, "seed");
    }

    protected Iterable<Generator<?>> auxiliaryGeneratorSource() {
        return null;
    }

    protected abstract void primeSourceOfRandomness() throws Exception;

    protected final AnnotatedType annotatedType() throws Exception {
        try {
            return typeBearerField().getAnnotatedType();
        } catch (Exception e) {
            return typeBearerParameter().getAnnotatedType();
        }
    }

    protected final AnnotatedElement annotatedElement() throws Exception {
        try {
            return typeBearerField();
        } catch (Exception e) {
            return typeBearerParameter();
        }
    }

    private Field typeBearerField() throws Exception {
        return getClass().getField("TYPE_BEARER");
    }

    private Parameter typeBearerParameter() throws Exception {
        return getClass().getMethod("TYPE_BEARER", Integer.class).getParameters()[0];
    }

    protected abstract int sampleSize();

    protected abstract List<?> randomValues();

    @Test public final void respectsSampleSize() {
        assertEquals(quantifier.sampleSize(), theoryParameters.size());
    }

    @Test public final void insertsTheRandomValuesIntoAssignments() throws Exception {
        assertEquals(sampleSize(), theoryParameters.size());

        List<?> values = randomValues();
        assertEquals(sampleSize(), values.size());

        for (int i = 0; i < theoryParameters.size(); ++i)
            verifyEquivalenceOfTheoryParameter(i, values.get(i), theoryParameters.get(i).getValue());
    }

    protected void verifyEquivalenceOfTheoryParameter(int index, Object expected, Object actual) {
        assertThat(index + "'th value", expected, deepEquals(actual));
    }

    @Test public abstract void verifyInteractionWithRandomness();
}
