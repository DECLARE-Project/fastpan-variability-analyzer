package de.fakeller.performance.variability.approaches.guo2013;

import de.fakeller.performance.analysis.result.PerformanceResult;

import java.util.Optional;

/**
 * Reduces a {@link PerformanceResult} to a simple score.
 */
@FunctionalInterface
public interface DecisionTreeObjective {

    /**
     * Assigns a score to a {@link PerformanceResult} as objective for the {@link DecisionTreeAnalyzer}.
     * <p>
     * Note: If the returned optional is empty, the configuration will be skipped.
     */
    Optional<Double> toObjective(PerformanceResult<?> result);
}
