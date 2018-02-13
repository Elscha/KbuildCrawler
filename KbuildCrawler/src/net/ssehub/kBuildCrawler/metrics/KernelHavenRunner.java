package net.ssehub.kBuildCrawler.metrics;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.ssehub.kBuildCrawler.git.FailureTrace;
import net.ssehub.kBuildCrawler.git.FileDefect;
import net.ssehub.kBuildCrawler.git.plugins.IGitPlugin;
import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.analysis.IAnalysisObserver;
import net.ssehub.kernel_haven.analysis.ObservableAnalysis;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.metric_haven.metric_components.AllFunctionMetrics;
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;
import net.ssehub.kernel_haven.util.Logger;

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
    
    public List<List<MultiMetricResult>> run(IGitPlugin git, FailureTrace ftrace) {
        System.out.println("--------------------------------");
        System.out.println("Running KernelHaven metrics for:\n" + ftrace);
        
        List<List<MultiMetricResult>> result = null;
        
        System.out.println("Restoring commit...");
        File sourceTree = git.restoreCommit(ftrace.getGitInfo());
        if (sourceTree != null) {
            System.out.println("Source tree checked out at " + sourceTree);
            
            result = new ArrayList<>(ftrace.getDefects().size());
            for (FileDefect defect : ftrace.getDefects()) {
                try {
                    run(sourceTree, defect);
                    result.add(analysisResult);
                } catch (IOException | SetUpException e) {
                    e.printStackTrace();
                }
            }
            
        } else {
            System.out.println("Unable to restore commit");
        }
        
        return result;
    }
    
    private void run(File sourceTree, FileDefect defect) throws IOException, SetUpException {
        // reset
        analysisResult = null;
        
        System.out.println("Running for defect: " + defect);
        System.out.println("---");
        
        if (Logger.get() == null) {
            Logger.init();
        }
        
        Properties props = new Properties();
        props.load(new FileReader(new File("res/metric_base.properties")));
        
        props.setProperty("source_tree", sourceTree.getAbsolutePath());
        props.setProperty("code.extractor.files", defect.getPath() + defect.getFile());
        props.setProperty("analysis.code_function.line", String.valueOf(defect.getLine()));
        
        Configuration config = new Configuration(props);
        DefaultSettings.registerAllSettings(config);
        
        AllFunctionMetrics.ADD_OBSERVEABLE = true;
        AllFunctionMetrics.ADD_LINE_FILTER = true;
        ObservableAnalysis.setObservers(this);
        
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
