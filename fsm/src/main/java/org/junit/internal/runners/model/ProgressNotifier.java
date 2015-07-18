package org.junit.internal.runners.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.contrib.theories.PotentialAssignment;
import org.junit.contrib.theories.internal.Assignments;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gabriel Ki <gabeki@apple.com>
 */
public class ProgressNotifier extends EachTestNotifier {

    private static Logger logger = LoggerFactory.getLogger(ProgressNotifier.class);

    public static class Step implements Callable<Void> {
        private FrameworkMethod method;
        private Assignments assignments;

        public Step(final FrameworkMethod method, final Assignments assignments) {
            this.method = method;
            this.assignments = assignments;
        }

        /**
         * Call the tested method with the given arguments again.
         *
         * TODO: Revisit me - depends how {@link org.junit.contrib.theories.GraphTheories.RerunnableScheduler} uses it.
         *
         * @return
         * @throws Exception
         */
        @Override
        public Void call() throws Exception {
            try {
                method.invokeExplosively(null, assignments.getMethodArguments());
            } catch (Throwable t) {
                // *sigh* this is why people shouldn't throw generic exceptions
                throw new RuntimeException(t);
            }
            return null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(method.getName());
            sb.append("(");

            Object[] params;
            try {
                params = assignments.getMethodArguments();
            } catch (PotentialAssignment.CouldNotGenerateValueException e) {
                // please forgive me
                return method.toString();
            }

            List<Type> paramTypes = Arrays.asList(method.getMethod().getParameterTypes());

            final AtomicInteger i = new AtomicInteger();
            sb.append(paramTypes.stream()
                    .map(t -> "(" + t + ") " + params[i.getAndIncrement()])
                    .collect(Collectors.joining(", ")));

            sb.append(")");
            return sb.toString();
        }
    }

    private List<Step> progress;
    private Description description;

    public ProgressNotifier(RunNotifier notifier, Description description) {
        super(notifier, description);
        this.description = description;
        this.progress = new ArrayList<>();
    }

    public Description description() {
        return description;
    }

    public void addStep(final FrameworkMethod testedMethod, final Assignments usedAssignments) {
        progress.add(new Step(testedMethod, usedAssignments));
    }

    public void addFailedAssumption(AssumptionViolatedException e) {
        super.addFailedAssumption(e);
        if (logger.isErrorEnabled()) {
            logger.error("Failed Sequence " + this, e);
        }
    }

    @Override
    public String toString() {
        return toString(" -> ");
    }

    public String toString(final String delimiter) {
        final String prefix = description.getClassName() + ".";
        return description + ": "
                + progress.stream()
                          .map(i -> i.toString().replace(prefix, ""))
                          .collect(Collectors.joining(delimiter));
    }
}

