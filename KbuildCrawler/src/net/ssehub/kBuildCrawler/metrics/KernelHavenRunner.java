package net.ssehub.kBuildCrawler.metrics;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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
    
    private List<MultiMetricResult> analysisResult;
    
    public @Nullable List<MultiMetricResult> run(GitInterface git, FailureTrace ftrace) {
        System.out.println("--------------------------------");
        System.out.println("Running KernelHaven metrics for:\n" + ftrace);
        
        List<MultiMetricResult> result = null;
        
        try {
            System.out.println("Restoring commit...");
            git.restoreCommit(ftrace.getGitInfo());
            
            System.out.println("Source tree checked out at " + git.getSourceTree());
            
            result = new LinkedList<>();
            
            try {
                runNonFilterableMetrics(git.getSourceTree());
                result.addAll(analysisResult);
            } catch (IOException | SetUpException e) {
                e.printStackTrace();
            }
            
            for (FileDefect defect : ftrace.getDefects()) {
                try {
                    runLineFilteredMetrics(git.getSourceTree(), defect);
                    result.addAll(analysisResult);
                } catch (IOException | SetUpException e) {
                    e.printStackTrace();
                }
            }
            
        } catch (GitException e) {
            System.out.println("Unable to restore commit:");
            e.printStackTrace(System.out);
        }
        
        return result;
    }
    
    private void runNonFilterableMetrics(File sourceTree) throws IOException, SetUpException {
        // reset
        analysisResult = null;
        
        System.out.println("Running on full tree: " + sourceTree);
        System.out.println("---");
        
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
        
        System.out.println("Running for defect: " + defect);
        System.out.println("---");
        
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
        
        System.out.println("---");
        
        if (analysisResult == null) {
            System.out.println("Got no result... :-/");
        } else {
            System.out.println("Got " + analysisResult.size() + " multi metric results");
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
    
}
