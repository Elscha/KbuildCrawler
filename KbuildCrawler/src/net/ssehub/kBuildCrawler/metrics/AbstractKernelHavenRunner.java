package net.ssehub.kBuildCrawler.metrics;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import net.ssehub.kernel_haven.metric_haven.MetricResult;
import net.ssehub.kernel_haven.metric_haven.multi_results.MeasuredItem;
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
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
            System.err.println("  Restoring commit took " + duration + "ms");
            
            LOGGER.logInfo("Source tree checked out at " + git.getSourceTree());
            
            if (!checkIfNeedToRun(git.getSourceTree(), ftrace.getDefects())) {
                LOGGER.logInfo("Don't need to run this, because no defect lies within a function");
                
                return result;
            }
            
            List<MultiMetricResult> completeTree = null;
            List<MultiMetricResult> functionMetrics = new LinkedList<>();
            
            t0 = System.currentTimeMillis();
            try {
                completeTree = runNonFilterableMetrics(git.getSourceTree(), ftrace.getDefects());
            } catch (IOException | SetUpException e) {
                LOGGER.logException("Exception while running on whole tree", e);
            }
            duration = System.currentTimeMillis() - t0;
            System.err.println("  Running non-filterable metrics took " + duration + "ms");
            
            t0 = System.currentTimeMillis();
            try {
                List<MultiMetricResult> filteredResults = runLineFilteredMetrics(git.getSourceTree(), ftrace.getDefects());
                if (filteredResults != null) {
                    functionMetrics = filteredResults;
                }
            } catch (IOException | SetUpException e) {
                LOGGER.logException("Exception while running on whole single function", e);
            }
            duration = System.currentTimeMillis() - t0;
            System.err.println("  Running filterable metrics took " + duration + "ms");

            t0 = System.currentTimeMillis();
            result = joinFunctionAndCompleteMetricResults(completeTree, functionMetrics);
            duration = System.currentTimeMillis() - t0;
            System.err.println("  Joining MultiMetricResults took " + duration + "ms");
            
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
     * @param defects The metric will only executed on files described by these defects.
     * 
     * @return The metric results
     * @throws IOException
     * @throws SetUpException
     */
    protected abstract List<MultiMetricResult> runLineFilteredMetrics(File sourceTree, List<FileDefect> defects)
        throws IOException, SetUpException;
    
    
    /**
     * Checks if any of the given defects lies within a function. If this is not the case, then we don't need to run
     * our metrics.
     * 
     * @param defects The defects to check.
     * @return Whether any of the defects lies within a function
     */
    private boolean checkIfNeedToRun(File sourceTree, List<FileDefect> defects) {
        IsFunctionChecker checker = new IsFunctionChecker(sourceTree);
        
        for (FileDefect defect : defects) {
            
            if (checker.isWithinFunction(new File(defect.getFile()), defect.getLine())) {
                return true;
            }
            
        }
        
        return false;
    }
    
    /**
     * <p>
     * Does a right-join on the metric results for the complete tree and the metric results for the filtered functions.
     * This means, that the result list will contain all functions that are in the functionMetrics list, with the
     * added metric values from completeTree.
     * </p>
     * 
     * Package visibility for test cases.
     * 
     * @throws IllegalArgumentException If functionMetrics contains two {@link MetricResult}s for the same
     *      {@link MeasuredItem}.
     */
    static List<MultiMetricResult> joinFunctionAndCompleteMetricResults(List<MultiMetricResult> completeTree,
            List<MultiMetricResult> functionMetrics) throws IllegalArgumentException {
        
        List<MultiMetricResult> result = new LinkedList<>();
        
        if (completeTree == null || completeTree.size() == 0) {
            result = functionMetrics;
            
        } else {
            /*
             * Collect all single function metrics
             */
            Map<MeasuredItem, MultiMetricResult> functionMap = new HashMap<>();
            for (MultiMetricResult metricResult : functionMetrics) {
                MeasuredItem mi = metricResult.getMeasuredItem();
                
                if (functionMap.containsKey(mi)) {
                    throw new IllegalArgumentException("Found multiple filtered function results for: " + mi.toString());
                }
                
                functionMap.put(mi, metricResult);
            }
            
            /*
             * Go through the completeTree results and pick all functions that we have already seen in the single function metrics
             */
            for (MultiMetricResult metricResult : completeTree) {
                MeasuredItem mi = metricResult.getMeasuredItem();
                
                if (functionMap.containsKey(mi)) {
                    
                    MultiMetricResult singleFunctionResult = functionMap.get(mi);
                    
                    result.add(joinMultiResult(singleFunctionResult, metricResult));
                }
            }
        }
        
        return result;
    }
    
    /**
     * <p>
     * Joins the two lists of {@link MultiMetricResult}s together. This is a full join, i.e. the resulting list will
     * contain all functions from both lists with the metric values combined.
     * </p>
     * 
     * <p>
     * This method assumes that the columns in the left and right list are consistent (respectively). That means,
     * all entries in l1 should have the same number of columns with the same metrics in the same order (the same
     * should go for l2) (this does not mean that l1 and l2 should contain the same metrics).   
     * </p>
     * 
     * <p>
     * If one of the lists contains results for a {@link MeasuredItem} that is not in the other one, the "empty cells"
     * are filled with <code>null</code> in the resulting {@link MultiMetricResult}.
     * </p>
     * 
     * <p>
     * This method assumes that each list contains at most one instance for each {@link MeasuredItem}. That means,
     * each {@link MeasuredItem} may only appear once in l1, and once in l2.
     * </p>
     * 
     * @throws IllegalArgumentException If the same {@link MeasuredItem} appears twice in the same list; or if the
     *      {@link MultiMetricResult} in a list have different metrics; or if merging
     *      two {@link MultiMetricResult}s fails for other reasons.
     */
    static List<MultiMetricResult> joinFullMetricResults(List<MultiMetricResult> l1, List<MultiMetricResult> l2) {
        // quick path: if either list is empty, return the other one:
        if (l1.isEmpty()) {
            return l2;
        }
        if (l2.isEmpty()) {
            return l1;
        }
        
        /*
         * Create dummy null arrays to join with if either list does not contain an element 
         */
        @NonNull String[] l1Metrics = l1.get(0).getMetrics();
        @NonNull String[] l2Metrics = l2.get(0).getMetrics();
        @Nullable Double[] l1NullValues = new @Nullable Double[l1Metrics.length];
        @Nullable Double[] l2NullValues = new @Nullable Double[l2Metrics.length];
        
        Map<MeasuredItem, MultiMetricResult> m1 = new HashMap<>();
        Map<MeasuredItem, MultiMetricResult> m2 = new HashMap<>();
        
        /*
         * First stage: collect everything into maps
         */
        
        for (MultiMetricResult metricResult : l1) {
            if (!Arrays.equals(l1Metrics, metricResult.getMetrics())) {
                throw new IllegalArgumentException("Elements in l1 contain different metric header (expected: "
                        + Arrays.toString(l1Metrics) + " but got " + Arrays.toString(metricResult.getMetrics()) + ")");
            }
            
            MeasuredItem mi = metricResult.getMeasuredItem();
            
            if (m1.containsKey(mi)) {
                throw new IllegalArgumentException("Found the same MeasuredItem multiple times in l1: " + mi);
            }
            
            m1.put(mi, metricResult);
        }
        
        for (MultiMetricResult metricResult : l2) {
            if (!Arrays.equals(l2Metrics, metricResult.getMetrics())) {
                throw new IllegalArgumentException("Elements in l2 contain different metric header (expected: "
                        + Arrays.toString(l2Metrics) + " but got " + Arrays.toString(metricResult.getMetrics()) + ")");
            }
            
            MeasuredItem mi = metricResult.getMeasuredItem();
            
            if (m2.containsKey(mi)) {
                throw new IllegalArgumentException("Found the same MeasuredItem multiple times in l2: " + mi);
            }
            
            m2.put(mi, metricResult);
        }

        /*
         * Second stage: join everything together
         */
        List<MultiMetricResult> result = new LinkedList<>();
        
        for (Map.Entry<MeasuredItem, MultiMetricResult> entry : m1.entrySet()) {
            MeasuredItem mi = entry.getKey();
            
            MultiMetricResult otherEntry = m2.get(mi);
            if (otherEntry != null) {
                // we have this function in both lists: join
                result.add(joinMultiResult(entry.getValue(), otherEntry));
                m2.remove(mi); // remove element
                
            } else {
                // we only have this function in l1: join with null
                result.add(joinMultiResult(entry.getValue(), new MultiMetricResult(mi, l2Metrics, l2NullValues)));
            }
        }
        
        // everything that is left are elements that are in m2 but not in m1
        for (Map.Entry<MeasuredItem, MultiMetricResult> entry : m2.entrySet()) {
            MeasuredItem mi = entry.getKey();
            
            // join with null
            result.add(joinMultiResult(new MultiMetricResult(mi, l1Metrics, l1NullValues), entry.getValue()));
        }
        
        return result;
    }
    
    /**
     * Joins the two {@link MultiMetricResult}s. This method basically just combines the metric and values arrays of
     * two {@link MultiMetricResult}s into one new {@link MultiMetricResult}.
     * 
     * @throws IllegalArgumentException If the two {@link MultiMetricResult}s don't have the same {@link MeasuredItem}.
     */
    private static MultiMetricResult joinMultiResult(MultiMetricResult r1, MultiMetricResult r2)
            throws IllegalArgumentException {
        
        if (!r1.getMeasuredItem().equals(r2.getMeasuredItem())) {
            throw new IllegalArgumentException("The two MultiMetricResults to join don't have the same MeasuredItems "
                    + "(" + r1.getMeasuredItem() + " vs. " + r2.getMeasuredItem() + ")");
        }
        
        @NonNull String[] metrics1 = r1.getMetrics();
        @NonNull String[] metrics2 = r2.getMetrics();
        
        @NonNull String[] combinedMetrics = new @NonNull String[metrics1.length + metrics2.length];
        System.arraycopy(metrics1, 0, combinedMetrics, 0, metrics1.length);
        System.arraycopy(metrics2, 0, combinedMetrics, metrics1.length, metrics2.length);
        
        @Nullable Double[] values1 = r1.getValues();
        @Nullable Double[] values2 = r2.getValues();
        
        @Nullable Double[] combinedValues = new @Nullable Double[values1.length + values2.length];
        System.arraycopy(values1, 0, combinedValues, 0, values1.length);
        System.arraycopy(values2, 0, combinedValues, values1.length, values2.length);
        
        return new MultiMetricResult(r1.getMeasuredItem(), combinedMetrics, combinedValues);
    }
    
    /**
     * <p>
     * Tries to convert the given multiResult in a way that it matches the expected header. If the order of the entries
     * is different, they are reordered. If entries are missing, <code>null</code> elements are added in their place.
     * </p>
     * <p>
     * If the {@link MultiMetricResult} contains metrics that are not in the expected header, the result can't be
     * converted and an {@link IllegalArgumentException} is thrown.
     * </p>
     * 
     * @param expectedMetrics The expected list of metrics.
     * @param multiResult The {@link MultiMetricResult} to convert to the expected header.
     * 
     * @return A fixed {@link MultiMetricResult}. (no-op if the multiResult already matches expectedMetrics)
     * 
     * @throws IllegalArgumentException If the multiResult can't be adapted to the expected metrics.
     */
    public static MultiMetricResult tryFixToFormat(String[] expectedMetrics, MultiMetricResult multiResult)
        throws IllegalArgumentException {
        
        MultiMetricResult result;
        
        if (Arrays.equals(expectedMetrics, multiResult.getMetrics())) {
            // no problem, everything matches :-)
            result = multiResult;
            
        } else {
            
            // convert both metric lists to sets
            Set<String> expectedMetricSet = new HashSet<>();
            for (String expectedMetric : expectedMetrics) {
                boolean newEntry = expectedMetricSet.add(expectedMetric);
                if (!newEntry) {
                    throw new IllegalArgumentException("Found same metric name twice: " + expectedMetric);
                }
            }
            
            Set<String> actualMetricSet = new HashSet<>();
            for (String actualMetric : multiResult.getMetrics()) {
                boolean newEntry = actualMetricSet.add(actualMetric);
                if (!newEntry) {
                    throw new IllegalArgumentException("Found same metric name twice: " + actualMetric);
                }
            }
            
            // check that actualMetricSet is a subset of expectedMetricSet
            if (!expectedMetricSet.containsAll(actualMetricSet)) {
                actualMetricSet.removeAll(expectedMetricSet);
                throw new IllegalArgumentException("Found new metrics that are not in the expected list: " + actualMetricSet);
            }
            
            // convert actual result to a map
            Map<String, Double> actualMetricMap = new HashMap<>();
            for (int i = 0; i < multiResult.getMetrics().length; i++) {
                actualMetricMap.put(multiResult.getMetrics()[i], multiResult.getValues()[i]);
            }
            
            // build a new MultiMetricResult in correct order, and with null values filled
            String[] newMetrics = new String[expectedMetrics.length];
            Double[] newValues = new Double[expectedMetrics.length];
            for (int i = 0; i < expectedMetrics.length; i++) {
                String metric = expectedMetrics[i];
                newMetrics[i] = metric;
                newValues[i] = actualMetricMap.getOrDefault(metric, null);
            }
            
            result = new MultiMetricResult(multiResult.getMeasuredItem(), newMetrics, newValues);
        }
        
        return result;
    }
    
}
