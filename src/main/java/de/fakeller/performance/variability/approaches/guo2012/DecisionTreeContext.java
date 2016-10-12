package de.fakeller.performance.variability.approaches.guo2012;

import de.fakeller.performance.analysis.AnalysisContext;
import de.fakeller.performance.analysis.PerformanceAnalyzer;
import de.fakeller.performance.analysis.result.PerformanceResult;
import de.fakeller.performance.variability.SystemProvider;
import de.fakeller.performance.variability.VariabilityContext;
import de.fakeller.performance.variability.configuration.Configuration;
import de.fakeller.performance.variability.configuration.ConfigurationProvider;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.DecisionTree;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * See {@link DecisionTreeAnalyzer} for a description.
 *
 * @author Fabian Keller
 */
public class DecisionTreeContext<SYSTEM, FEATURE> implements VariabilityContext<SYSTEM, FEATURE> {

    // // DEPENDENCIES // //

    private final SystemProvider<SYSTEM, FEATURE> systemProvider;
    private final ConfigurationProvider<FEATURE> configurationProvider;
    private final PerformanceAnalyzer<SYSTEM, ? extends AnalysisContext<SYSTEM>> analyzer;
    private final Function<PerformanceResult<?>, Double> objective;
    private final JavaSparkContext sc;


    // // RESULTS // //

    private DecisionTreeModel model;


    public DecisionTreeContext(final SystemProvider<SYSTEM, FEATURE> systemProvider, final ConfigurationProvider<FEATURE> configurationProvider, final PerformanceAnalyzer analyzer, final Function<PerformanceResult<?>, Double> objective, final JavaSparkContext sc) {
        this.systemProvider = systemProvider;
        this.configurationProvider = configurationProvider;
        this.analyzer = analyzer;
        this.objective = objective;
        this.sc = sc;
    }

    @Override
    public void analyze() {
        // 1. analyze performance for all variants
        final Map<Configuration<FEATURE>, PerformanceResult<?>> results = new HashMap<>();
        this.configurationProvider.configurations().forEachRemaining(config -> {
            final SYSTEM system = this.systemProvider.systemFor(config);
            final PerformanceResult<?> result = this.analyzer.setupAnalysis(system).analyze();
            results.put(config, result);
        });

        // 2. map results with objective function
        final Map<Configuration<FEATURE>, Double> scoredResults = results
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> this.objective.apply(e.getValue())));

        // 3. build decision tree
        final List<LabeledPoint> points = scoredResults.entrySet().stream()
                .map(se -> new LabeledPoint(se.getValue(), this.configurationToVector(se.getKey())))
                .collect(Collectors.toList());
        final JavaRDD<LabeledPoint> trainingData = this.sc.parallelize(points);
        final Map<Integer, Integer> categoricalFeaturesInfo = new HashMap<>();
        final String impurity = "variance";
        final int maxDepth = 4;
        final int maxBins = 32;

        this.model = DecisionTree.trainRegressor(trainingData, categoricalFeaturesInfo, impurity, maxDepth, maxBins);
    }

    private Vector configurationToVector(final Configuration<FEATURE> cfg) {
        final List<Boolean> flags = cfg.getFeatureFlags();
        final double[] vector = new double[flags.size()];
        for (int i = 0; i < flags.size(); i++) {
            vector[i] = flags.get(i) ? 1.0 : 0.0;
        }
        return Vectors.dense(vector);
    }

    public DecisionTreeModel getModel() {
        assert null != this.model;
        return this.model;
    }

    public Map<Configuration<FEATURE>, Double> predict(final ConfigurationProvider<FEATURE> configurations) {
        final ArrayList<Configuration<FEATURE>> configs = new ArrayList<>();
        // generate prediction input from feature configuration
        final List<Vector> predictionInputs = new ArrayList<>();
        final Iterator<Configuration<FEATURE>> cfgs = configurations.configurations();
        while (cfgs.hasNext()) {
            final Configuration<FEATURE> cfg = cfgs.next();
            configs.add(cfg);
            predictionInputs.add(this.configurationToVector(cfg));
        }

        // predict
        final JavaRDD<Vector> input = this.sc.parallelize(predictionInputs);
        final JavaRDD<Double> prediction = this.model.predict(input);

        // create return map
        final Map<Configuration<FEATURE>, Double> res = new HashMap<>();
        prediction.collect().forEach(p -> {
            final Configuration<FEATURE> cfg = configs.remove(0);
            res.put(cfg, p);
        });
        return res;
    }
}
