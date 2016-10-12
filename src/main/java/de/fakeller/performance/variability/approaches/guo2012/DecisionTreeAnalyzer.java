package de.fakeller.performance.variability.approaches.guo2012;

import de.fakeller.performance.analysis.AnalysisCapability;
import de.fakeller.performance.analysis.PerformanceAnalyzer;
import de.fakeller.performance.analysis.result.PerformanceResult;
import de.fakeller.performance.variability.SystemProvider;
import de.fakeller.performance.variability.VariabilityAnalyzer;
import de.fakeller.performance.variability.configuration.Configuration;
import de.fakeller.performance.variability.configuration.ConfigurationProvider;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Set;
import java.util.function.Function;

/**
 * This analyzer is an implementation of the approach presented by Guo et al. in 2012:
 * "Variability-Aware Performance Prediction: A Statistical Learning Approach"
 * <p>
 * The approach leverages decision trees to build a model using only a small set of training data. The model is then
 * capable of predicting the performance of unseen configurations.
 * <p>
 * This decision tree analyzer provides two results:
 * - a classifier to predict the performance of an unseen {@link Configuration}
 * - access to the model, as it contains information on how the features interact
 */
public class DecisionTreeAnalyzer<SYSTEM, FEATURE> implements VariabilityAnalyzer<SYSTEM, DecisionTreeContext<SYSTEM, FEATURE>, FEATURE> {

    private final PerformanceAnalyzer analyzer;
    private final Function<PerformanceResult<?>, Double> objective;
    private final JavaSparkContext sparkContext;

    /**
     * @param analyzer     the performance analyzer to use to create results for each configuration
     * @param objective    an objective function that assigns a double score to a performance result. The score is used to train the decision tree.
     * @param sparkContext the spark context to use
     */
    public DecisionTreeAnalyzer(final PerformanceAnalyzer analyzer, final Function<PerformanceResult<?>, Double> objective, final JavaSparkContext sparkContext) {
        this.analyzer = analyzer;
        this.objective = objective;
        this.sparkContext = sparkContext;
    }

    @Override
    public Set<AnalysisCapability> capabilities() {
        return null;
    }

    @Override
    public DecisionTreeContext<SYSTEM, FEATURE> setupAnalysis(final SystemProvider<SYSTEM, FEATURE> systemProvider, final ConfigurationProvider<FEATURE> configurationProvider) {
        return new DecisionTreeContext<SYSTEM, FEATURE>(systemProvider, configurationProvider, this.analyzer, this.objective, this.sparkContext);
    }
}
