package de.fakeller.performance.variability.approaches.guo2012;

import de.fakeller.performance.analysis.AnalysisCapability;
import de.fakeller.performance.analysis.AnalysisContext;
import de.fakeller.performance.analysis.PerformanceAnalyzer;
import de.fakeller.performance.analysis.result.AbstractPerformanceResult;
import de.fakeller.performance.analysis.result.AttachedResult;
import de.fakeller.performance.analysis.result.PerformanceResult;
import de.fakeller.performance.analysis.result.metric.AbstractPerformanceMetric;
import de.fakeller.performance.analysis.result.valueobject.ValueObject;
import de.fakeller.performance.variability.configuration.Configuration;
import de.fakeller.performance.variability.configuration.ConfigurationProvider;
import de.fakeller.performance.variability.feature.Feature;
import de.fakeller.performance.variability.feature.FeatureModel;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fabian Keller
 */
@Ignore(value = "Test used to reproduce results of Guo et al., 2012.")
public class DecisionTreeAnalyzerTest {


    @Test
    public void setupAnalysis() throws Exception {
        final SparkConf cfg = new SparkConf().setAppName("testing").setMaster("local");
        final JavaSparkContext sc = new JavaSparkContext(cfg);

        final LoadData data = new LoadData("src/test/resources/BDBCAll.csv", 18 * 3);

        final DecisionTreeAnalyzer<String, String> analyzer = new DecisionTreeAnalyzer<String, String>(new StubAnalyzer(data), res -> {
            final AttachedResult<String> result = (AttachedResult<String>) ((PerformanceResult<String>) res).getResults("result").toArray()[0];
            final DoubleMetric metric = (DoubleMetric) result.value();
            return metric.getDouble();
        }, sc);
        final DecisionTreeContext<String, String> context = analyzer.setupAnalysis(
                config -> config.toString(),
                data.getTrain()
        );
        context.analyze();
        final Map<Configuration<String>, Double> predicted = context.predict(data.getPredict());

        final Optional<Double> sumFR = predicted.entrySet().stream()
                .map(s -> {
                    final double actual = data.performanceFor(s.getKey().toString());
                    return Math.abs(actual - s.getValue()) / actual;
                })
                .reduce((a, b) -> a + b);
        System.out.println("Fault Rate: " + sumFR.get() / predicted.values().size());

        context.getModel();

    }


    private class StubAnalyzer implements PerformanceAnalyzer<String, StubContext> {

        private final LoadData data;

        public StubAnalyzer(final LoadData data) {
            this.data = data;
        }

        @Override
        public Set<AnalysisCapability> capabilities() {
            return new HashSet<>();
        }

        @Override
        public boolean supports(final String s) {
            return true;
        }

        @Override
        public StubContext setupAnalysis(final String system) {
            return new StubContext(this.data, system);
        }
    }

    private class StubContext implements AnalysisContext<String> {

        private final LoadData data;
        private final String system;

        public StubContext(final LoadData loadData, final String system) {
            this.data = loadData;
            this.system = system;
        }

        @Override
        public PerformanceResult<String> analyze() {
            final AbstractPerformanceResult<String> res = new AbstractPerformanceResult<String>() {
            };
            res.attach(new AttachedResult<String>("result", new DoubleMetric(new DoubleVO(this.data.performanceFor(this.system)))));
            return res;
        }
    }

    private class LoadData {

        private final int N;
        private final List<Configuration<String>> cfgs;

        private final List<Configuration<String>> train;
        private final List<Configuration<String>> predict;

        private final Map<String, Double> performance = new HashMap<>();

        public LoadData(final String fileName) throws FileNotFoundException {
            this(fileName, 18);
        }

        public LoadData(final String fileName, final int N) throws FileNotFoundException {
            this.N = N;
            final Scanner scanner = new Scanner(new File(fileName));
            scanner.useDelimiter("\n");
            final List<String[]> lines = new ArrayList<>();
            while (scanner.hasNext()) {
                lines.add(scanner.next().split(","));
            }
            scanner.close();

            final String[] header = lines.get(0);
            lines.remove(0);
            final List<Feature<String>> features = new ArrayList<>();
            for (int i = 0; i < header.length - 1; i++) {
                features.add(new Feature<>(header[i], header[i]));
            }
            final FeatureModel<String> fm = new FeatureModel<>(features);

            this.cfgs = lines.stream()
                    .map(flags -> {
                        final Configuration<String> cfg = new Configuration<>(fm);
                        // last flags entry stores actual performance
                        for (int i = 0; i < flags.length - 1; i++) {
                            if ("1".equals(flags[i])) {
                                cfg.enable(fm.get(i));
                            }
                        }
                        this.performance.put(String.valueOf(cfg.toString()), Double.valueOf(flags[flags.length - 1]));
                        return cfg;
                    })
                    .collect(Collectors.toList());
            Collections.shuffle(this.cfgs, new Random(System.nanoTime()));
            this.train = this.cfgs.subList(0, N);
            this.predict = this.cfgs.subList(N, this.cfgs.size());
        }

        public ConfigurationProvider<String> getTrain() {
            return this.train::iterator;
        }

        public ConfigurationProvider<String> getPredict() {
            return this.predict::iterator;
        }

        public double performanceFor(final String system) {
            assert this.performance.containsKey(system);
            return this.performance.get(system);
        }
    }


    class DoubleVO implements ValueObject {

        private final double val;

        public DoubleVO(final double val) {
            this.val = val;
        }

        @Override
        public String toHumanReadable() {
            return String.valueOf(this.val);
        }
    }

    class DoubleMetric extends AbstractPerformanceMetric<DoubleVO> {
        public DoubleMetric(final DoubleVO value) {
            super(value);
        }

        @Override
        protected String getHumanReadableDescription() {
            return "DoubleMetric::" + this.value.toHumanReadable();
        }

        public double getDouble() {
            return this.value.val;
        }
    }
}