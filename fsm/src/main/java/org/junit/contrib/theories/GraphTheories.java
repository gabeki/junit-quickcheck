package org.junit.contrib.theories;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.internal.runners.model.ProgressNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GraphTheories extends Theories {

    private static Logger logger = LoggerFactory.getLogger(GraphTheories.class);

    /**
     * Configuration for {@link GraphTheories}.
     */
    @Target(TYPE)
    @Retention(RUNTIME)
    public @interface ForAllTheories {
        /**
         * @return the minimum no. of theories should be run in a test.
         */
        int min() default 1;

        /**
         * @return the maximum no. of theories should be run in a test.
         */
        int max() default 10;

        /**
         * @return how long GraphTheories should run in time instead of no. of
         * test cases.  If this value is <= 0, only 1 test case would be run.
         */
        long sampleTime() default 0;

        /**
         * @return the unit of {@link #sampleTime()}.
         */
        TimeUnit sampleTimeUnit() default TimeUnit.SECONDS;

        /**
         * @return the setting for auto shrinkage for failed test cases.
         */
        boolean autoShrink() default false;

        /**
         * An instance that returns default values.  Used when user did not annotate
         * ForAllTheories at all.
         *
         *  @ForAllTheories final class C{}
         *  static final ForAllTheories DEFAULTS = C.class.getAnnotation(ForAllTheories.class);
         */
        static final ForAllTheories DEFAULTS = new ForAllTheories() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return ForAllTheories.class;
            }

            @Override
            public int min() {
                return 1;
            }

            @Override
            public int max() {
                return 10;
            }

            @Override
            public long sampleTime() {
                return 0;
            }

            @Override
            public TimeUnit sampleTimeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            public boolean autoShrink() {
                return false;
            }
        };
    }


    /**
     * Configuration of a Theory (Vertex).  If this is not given, the default values are used.
     */
    @Target(METHOD)
    @Retention(RUNTIME)
    public @interface TheoryVertex {
        /**
         * Allow this Theory to be the first to execute in the test.
         */
        boolean isStart() default false;

        TheoryEdge[] connectTo() default {};

        /**
         * Allow this Theory to be called 2 times in a row.
         */
        boolean loopToSelf() default true;

        /**
         * Weight itself.
         */
        double weight() default 1.0D;

        static final TheoryVertex DEFAULTS = new TheoryVertex() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return TheoryVertex.class;
            }

            @Override
            public boolean isStart() {
                return false;
            }

            @Override
            public boolean loopToSelf() {
                return true;
            }

            @Override
            public double weight() {
                return 1.0D;
            }

            @Override
            public TheoryEdge[] connectTo() {
                return new TheoryEdge[0];
            }
        };
    }

    /**
     * Configuration of a connecting Theory (Edge).
     */
    @Target(ANNOTATION_TYPE)
    @Retention(RUNTIME)
    public @interface TheoryEdge {
        /**
         * Simple or Fully qualified method name.
         */
        String name();

        double weight() default 1.0D;

        static final TheoryEdge DEFAULTS = new TheoryEdge() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return TheoryEdge.class;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public double weight() {
                return 1.0D;
            }
        };
    }

    private RerunnableScheduler scheduler = new RerunnableScheduler();

    private ForAllTheories conf;

    public GraphTheories(Class<?> clazz) throws InitializationError {
        super(clazz);

        // Configurations
        conf = clazz.getAnnotation(ForAllTheories.class);
        if (conf == null) {
            conf = ForAllTheories.DEFAULTS;
        }
    }

    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        // notifier.addListener(new FailureListener());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    // TODO: Implement filter children here - to only use @Theory not @Test or others
                    final FrameworkMethodGraph graph = FrameworkMethodGraph.parseFrameworkMethodGraph(getChildren());

                    // TODO: forking here?
                    if (conf.sampleTime() > 0) {
                        // generate as many tests as possible
                        Executors.newSingleThreadExecutor().submit(() -> {
                            final AtomicInteger i = new AtomicInteger();
                            while (true) {
                                createTest(graph, i.getAndIncrement(), notifier);
                            }
                        }).get(conf.sampleTime(), conf.sampleTimeUnit());
                    } else {
                        createTest(graph, 0, notifier);
                    }
                } catch (TimeoutException e) {
                    // swallow timeout
                } finally {
                    scheduler.finished();
                }
            }
        };
    }

    /**
     * Create and execute a test.
     *
     * @param count
     */
    private void createTest(final FrameworkMethodGraph graph, final int count, final RunNotifier notifier) {
        // TODO: Improve range (no. of theories) generation here
        final int length = new Random().nextInt(conf.max() - conf.min() + 1) + conf.min();
        final Description description = Description.createTestDescription(getTestClass().getJavaClass(), "Test #" + count + "(" + length + ")");
        final ProgressNotifier progress = new ProgressNotifier(notifier, description);
        scheduler.setProgressNotifier(progress).schedule(
                () -> runLeaf(new InvokeMethods(graph.asList(length), progress), description, notifier));
        if (logger.isDebugEnabled()) {
            logger.debug(progress.toString());
        }
    }


    private class InvokeMethods extends Statement {
        private final List<FrameworkMethod> testMethods;
        private final ProgressNotifier progress;

        public InvokeMethods(final List<FrameworkMethod> testMethods, ProgressNotifier notifier) {
            this.testMethods = testMethods;
            this.progress = notifier;
        }

        @Override
        public void evaluate() throws Throwable {
            for (FrameworkMethod testMethod : testMethods) {
                if (isIgnored(testMethod)) {
                    progress.fireTestIgnored();
                } else {
                    progress.addProgress(testMethod);
                    progress.description().addChild(describeChild(testMethod));
                    // TODO: need to capture generated params too
                    new TheoryAnchor(testMethod, getTestClass()).evaluate();
                }
            }
        }
    }

    /**
     * Re-runnable scheduler for scheduling shrinkage if required.
     */
    public static class RerunnableScheduler implements RunnerScheduler {

        private ProgressNotifier progress = null;

        public RerunnableScheduler setProgressNotifier(final ProgressNotifier progressNotifier) {
            progress = progressNotifier;
            return this;
        }

        @Override
        public void schedule(Runnable childStatement) {
            if (progress == null) {
                throw new ExceptionInInitializerError("ProgressNotifier is not set");
            }

            childStatement.run();
        }

        @Override
        public void finished() {
        }

        public void shrinkage() {
            // TODO: Implement me
        }
    }
}

