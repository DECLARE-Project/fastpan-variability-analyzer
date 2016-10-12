package de.fakeller.performance.variability.approaches.guo2013;

import de.fakeller.performance.analysis.result.PerformanceResult;

/**
 * Reduces a {@link PerformanceResult} to a simple score.
 */
@FunctionalInterface
public interface DecisionTreeObjective {

    /**
     * Assigns a score to a {@link PerformanceResult} as objective for the {@link DecisionTreeAnalyzer}.
     */
    double toObjective(PerformanceResult<?> result);
}
