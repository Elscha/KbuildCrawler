package net.ssehub.kBuildCrawler.metrics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.ssehub.kBuildCrawler.git.GitException;
import net.ssehub.kBuildCrawler.git.GitInterface;
import net.ssehub.kBuildCrawler.git.mail_parsing.FailureTrace;
import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Provides the basis to run metrics.
 * 
 * @author Adam
 */
public abstract class AbstractKernelHavenRunner {

    protected static final Logger LOGGER = Logger.get();
    
    public final @Nullable List<MultiMetricResult> run(GitInterface git, FailureTrace ftrace) {
        LOGGER.logInfo("--------------------------------", "Running KernelHaven metrics for:", ftrace.toString());
        
        List<MultiMetricResult> result = null;
        
        try {
            LOGGER.logInfo("Restoring commit...");
            long t0 = System.currentTimeMillis();
            git.restoreCommit(ftrace.getGitInfo(), ftrace.getFormattedDate(true));
            long duration = System.currentTimeMillis() - t0;
            System.err.println("Restoring commit took " + duration + "ms");
            
            LOGGER.logInfo("Source tree checked out at " + git.getSourceTree());
            
            List<MultiMetricResult> completeTree = null;
            List<List<MultiMetricResult>> functionMetrics = new LinkedList<>();
            
            t0 = System.currentTimeMillis();
            try {
                completeTree = runNonFilterableMetrics(git.getSourceTree(), ftrace.getDefects());
            } catch (IOException | SetUpException e) {
                LOGGER.logException("Exception while running on whole tree", e);
            }
            duration = System.currentTimeMillis() - t0;
            System.err.println("Running non-filterable metrics took " + duration + "ms");
            
            for (FileDefect defect : ftrace.getDefects()) {
                t0 = System.currentTimeMillis();
                try {
                    List<MultiMetricResult> filteredResults = runLineFilteredMetrics(git.getSourceTree(), defect);
                    if (filteredResults != null) {
                        functionMetrics.add(filteredResults);
                    }
                } catch (IOException | SetUpException e) {
                    LOGGER.logException("Exception while running on whole single function", e);
                }
                duration = System.currentTimeMillis() - t0;
                System.err.println("Running filterable metrics took " + duration + "ms");
            }

            t0 = System.currentTimeMillis();
            result = joinMultiMetricResults(completeTree, functionMetrics);
            duration = System.currentTimeMillis() - t0;
            System.err.println("Joining MultiMetricResults took " + duration + "ms");
            
        } catch (GitException e) {
            LOGGER.logException("Unable to restore commit", e);
        }
        
        return result;
    }
    
    /**
     * Executes all metrics, which must be executed on the source code of the <b>complete</b> product line.
     * <tt>defects</tt> may be used to filter the results <b>after</b> the metric execution.
     * @param sourceTree The root folder of the source tree to analyze
     * @param defects Optional list to filter the results after execution.
     * 
     * @return The metric results
     * @throws IOException
     * @throws SetUpException
     */
    protected abstract List<MultiMetricResult> runNonFilterableMetrics(File sourceTree, List<FileDefect> defects)
        throws IOException, SetUpException;
    
    /**
     * Executes all metrics, which can be executed on a <b>subset</b> of the source code.
     * <tt>defect</tt> are used to filter the results <b>before</b> the metric execution.
     * @param sourceTree The root folder of the source tree to analyze
     * @param defect The metric will only executed on files described by the defect.
     * 
     * @return The metric results
     * @throws IOException
     * @throws SetUpException
     */
    protected abstract List<MultiMetricResult> runLineFilteredMetrics(File sourceTree, FileDefect defect)
        throws IOException, SetUpException;
    
    
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
