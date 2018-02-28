package net.ssehub.kBuildCrawler.metrics;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.ssehub.kBuildCrawler.git.GitException;
import net.ssehub.kBuildCrawler.git.GitInterface;
import net.ssehub.kBuildCrawler.git.mail_parsing.FailureTrace;
import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;
import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.IAnalysisObserver;
import net.ssehub.kernel_haven.analysis.ObservableAnalysis;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.metric_haven.metric_components.AllFunctionMetrics;
import net.ssehub.kernel_haven.metric_haven.metric_components.AllLineFilterableFunctionMetrics;
import net.ssehub.kernel_haven.metric_haven.metric_components.AllNonLineFilterableFunctionMetrics;
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * <p>
 * Runs the {@link AllFunctionMetrics} of KernelHaven on the functions affected by a {@link FailureTrace}.
 * </p>
 * <p>
 * TODO: this is rather hackily implemented.
 * </p>
 * 
 * @author Adam
 */
public class KernelHavenRunner implements IAnalysisObserver {

    private static boolean pluginsLoaded = false;
    
    private static final Logger LOGGER = Logger.get();
    
    private List<MultiMetricResult> analysisResult;
    
    public @Nullable List<MultiMetricResult> run(GitInterface git, FailureTrace ftrace) {
        LOGGER.logInfo("--------------------------------", "Running KernelHaven metrics for:", ftrace.toString());
        
        List<MultiMetricResult> result = null;
        
        try {
            LOGGER.logInfo("Restoring commit...");
            git.restoreCommit(ftrace.getGitInfo(), ftrace.getFormattedDate(true));
            
            LOGGER.logInfo("Source tree checked out at " + git.getSourceTree());
            
            List<MultiMetricResult> completeTree = null;
            List<List<MultiMetricResult>> functionMetrics = new LinkedList<>();
            
            try {
                runNonFilterableMetrics(git.getSourceTree());
                completeTree = analysisResult;
            } catch (IOException | SetUpException e) {
                LOGGER.logException("Exception while running on whole tree", e);
            }
            
            for (FileDefect defect : ftrace.getDefects()) {
                try {
                    runLineFilteredMetrics(git.getSourceTree(), defect);
                    if (analysisResult != null) {
                        functionMetrics.add(analysisResult);
                    }
                } catch (IOException | SetUpException e) {
                    LOGGER.logException("Exception while running on whole single function", e);
                }
            }
            
            result = joinMultiMetricResults(completeTree, functionMetrics);
            
        } catch (GitException e) {
            LOGGER.logException("Unable to restore commit", e);
        }
        
        return result;
    }
    
    private void runNonFilterableMetrics(File sourceTree) throws IOException, SetUpException {
        // reset
        analysisResult = null;
        
        LOGGER.logInfo("Running on full tree: " + sourceTree);
        
        Properties props = new Properties();
        props.load(new FileReader(new File("res/metric_base.properties")));
        
        props.setProperty("source_tree", sourceTree.getAbsolutePath());
        props.setProperty("analysis.class", AllNonLineFilterableFunctionMetrics.class.getCanonicalName());
        
        AllNonLineFilterableFunctionMetrics.setAddObservable(true);
        ObservableAnalysis.setObservers(this);
        
        runKernelHaven(props);
    }
    
    private void runLineFilteredMetrics(File sourceTree, FileDefect defect) throws IOException, SetUpException {
        // reset
        analysisResult = null;
        
        LOGGER.logInfo("Running for defect: " + defect);
        
        Properties props = new Properties();
        props.load(new FileReader(new File("res/metric_base.properties")));
        
        props.setProperty("source_tree", sourceTree.getAbsolutePath());
        props.setProperty("analysis.class", AllLineFilterableFunctionMetrics.class.getCanonicalName());
        props.setProperty("code.extractor.files", defect.getPath() + defect.getFile());
        props.setProperty("analysis.code_function.line", String.valueOf(defect.getLine()));
        
        AllLineFilterableFunctionMetrics.setAddObservable(true);
        ObservableAnalysis.setObservers(this);
        
        runKernelHaven(props);
    }
    
    private void runKernelHaven(Properties props) throws SetUpException {
        Configuration config = new Configuration(props);
        DefaultSettings.registerAllSettings(config);
        
        LOGGER.logInfo("Running KernelHaven");
        LOGGER.setLevel(Level.WARNING);
        
        try {
            PipelineConfigurator pip = PipelineConfigurator.instance();
            pip.init(config);
            // don't call pip.execute() every time
            if (!pluginsLoaded) {
                pip.loadPlugins();
                pluginsLoaded = true;
            }
            
            pip.instantiateExtractors();
            pip.createProviders();
            pip.instantiateAnalysis();
            pip.runAnalysis();
            
        } finally {
            LOGGER.setLevel(Level.DEBUG);
        }
        
        if (analysisResult == null) {
            LOGGER.logWarning("Got no result... :-/");
        } else {
            LOGGER.logInfo("Got " + analysisResult.size() + " multi metric results");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void notifyFinished(List<?> analysisResults) {
        this.analysisResult = (List<MultiMetricResult>) analysisResults;
    }

    @Override
    public void notifyFinished() {
    }
    
    private static final class FunctionIdentifier {
        
        private static boolean HAS_INCLUDED_FILE = false;
        
        private static final int FILE_INDEX = 0;
        private static final int LINE_NUMBER_INDEX = 1;
        private static final int ELEMNET_INDEX = 2;
        
        private String file;
        private String lineNumber;
        private String element;
        
        public FunctionIdentifier(Object file, Object lineNumber, Object element) {
            this.file = file == null ? "" : file.toString();
            this.lineNumber = lineNumber == null ? "" : lineNumber.toString();
            this.element = element == null ? "" : element.toString();
        }
        
        public FunctionIdentifier(MultiMetricResult multiMetricResult) {
            this(multiMetricResult.getContent()[getFileIndex()], multiMetricResult.getContent()[getLineNumberIndex()],
                    multiMetricResult.getContent()[getElementIndex()]);
        }
        
        private static int getFileIndex() {
            return FILE_INDEX;
        }
        
        private static int getLineNumberIndex() {
            return HAS_INCLUDED_FILE ? LINE_NUMBER_INDEX + 1 : LINE_NUMBER_INDEX;
        }
        
        private static int getElementIndex() {
            return HAS_INCLUDED_FILE ? ELEMNET_INDEX + 1 : ELEMNET_INDEX;
        }
        
        @Override
        public boolean equals(Object obj) {
            boolean equal = false;
            if (obj instanceof FunctionIdentifier) {
                FunctionIdentifier other = (FunctionIdentifier) obj;
                equal = this.file.equals(other.file);
                equal = this.lineNumber.equals(other.lineNumber);
                equal = this.element.equals(other.element);
            }
            return equal;
        }
        
        @Override
        public int hashCode() {
            return file.hashCode() + lineNumber.hashCode() + element.hashCode();
        }
        
    }
    
    static List<MultiMetricResult> joinMultiMetricResults(List<MultiMetricResult> completeTree, List<List<MultiMetricResult>> functionMetrics) {
        
        List<MultiMetricResult> result = new LinkedList<>();
        
        if (completeTree == null || completeTree.size() == 0) {
            Set<FunctionIdentifier> functions = new HashSet<>();
            for (List<MultiMetricResult> metricsForSingleFunction : functionMetrics) {
                for (MultiMetricResult metricResult : metricsForSingleFunction) {
                    
                    FunctionIdentifier.HAS_INCLUDED_FILE = metricResult.getHeader()[1].equals("Included File");
                    FunctionIdentifier fi = new FunctionIdentifier(metricResult);
                    
                    if (!functions.contains(fi)) {
                        functions.add(fi);
                        result.add(metricResult);
                    }
                }
            }
            
        } else {
            /*
             * Join all single function metrics together
             */
            Map<FunctionIdentifier, MultiMetricResult> singleFunctionMetrics = new HashMap<>();
            for (List<MultiMetricResult> metricsForSingleFunction : functionMetrics) {
                for (MultiMetricResult metricResult : metricsForSingleFunction) {
                    
                    FunctionIdentifier.HAS_INCLUDED_FILE = metricResult.getHeader()[1].equals("Included File");
                    FunctionIdentifier fi = new FunctionIdentifier(metricResult);
                    singleFunctionMetrics.putIfAbsent(fi, metricResult);
                }
            }
            
            /*
             * Go through the completeTree results and pick all functions that we have already seen in the single function metrics
             */
            for (MultiMetricResult metricResult : completeTree) {
                FunctionIdentifier.HAS_INCLUDED_FILE = metricResult.getHeader()[1].equals("Included File");
                FunctionIdentifier fi = new FunctionIdentifier(metricResult);
                
                if (singleFunctionMetrics.containsKey(fi)) {
                    
                    MultiMetricResult singleFunctionResult = singleFunctionMetrics.get(fi);
                    
                    result.add(joinMultiResult(singleFunctionResult, metricResult));
                }
            }
        }
        
        return result;
    }
    
    private static MultiMetricResult joinMultiResult(MultiMetricResult r1, MultiMetricResult r2) {
        boolean r2HasIncludedFile = r2.getHeader()[1].equals("Included File");
        int r2MetricStartIndex = r2HasIncludedFile ? 4 : 3;
        
        String[] header = new String[r1.getHeader().length + r2.getHeader().length - r2MetricStartIndex];
        int headerIndex = 0;
        for (int i = 0; i < r1.getHeader().length; i++) {
            header[headerIndex++] = r1.getHeader()[i];
        }
        for (int i = r2MetricStartIndex; i < r2.getHeader().length; i++) {
            header[headerIndex++] = r2.getHeader()[i];
        }
        
        
        Object[] values = new Object[r1.getContent().length + r2.getContent().length - r2MetricStartIndex];
        int valuesIndex = 0;
        for (int i = 0; i < r1.getContent().length; i++) {
            values[valuesIndex++] = r1.getContent()[i];
        }
        for (int i = r2MetricStartIndex; i < r2.getContent().length; i++) {
            values[valuesIndex++] = r2.getContent()[i];
        }
        
        return new MultiMetricResult(header, values);
    }
    
}
