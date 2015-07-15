package org.junit.internal.runners.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private List<FrameworkMethod> progress;
    private Description description;

    public ProgressNotifier(RunNotifier notifier, Description description) {
        super(notifier, description);
        this.description = description;
        this.progress = new ArrayList<>();
    }

    public Description description() {
        return description;
    }

    public void addProgress(FrameworkMethod testedMethod) {
        progress.add(testedMethod);
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
                + progress.stream().map(i -> i.toString().replace(prefix, "")).collect(Collectors.joining(delimiter));
    }
}

