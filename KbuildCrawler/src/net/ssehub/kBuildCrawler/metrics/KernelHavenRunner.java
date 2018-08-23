package net.ssehub.kBuildCrawler.metrics;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

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
import net.ssehub.kernel_haven.util.Timestamp;
import net.ssehub.kernel_haven.util.Util;

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
class KernelHavenRunner extends AbstractKernelHavenRunner implements IAnalysisObserver {

    private static boolean pluginsLoaded = false;
    
    private static final Logger LOGGER = Logger.get();
    
    private List<MultiMetricResult> analysisResult;
    
    @Override
    protected List<MultiMetricResult> runNonFilterableMetrics(File sourceTree, List<FileDefect> defect)
        throws IOException, SetUpException {
        
        // reset
        analysisResult = null;
        
        LOGGER.logInfo("Running on full tree: " + sourceTree);
        
        Properties props = new Properties();
        props.load(new FileReader(new File("res/metric_base.properties")));
        
        props.setProperty("source_tree", sourceTree.getAbsolutePath());
        props.setProperty("analysis.class", AllNonLineFilterableFunctionMetrics.class.getCanonicalName());
        
        AllNonLineFilterableFunctionMetrics.setAddObservable(true);
        ObservableAnalysis.setObservers(this);
        
        return runKernelHaven(props);
    }
    
    @Override
    protected List<MultiMetricResult> runLineFilteredMetrics(File sourceTree, FileDefect defect)
        throws IOException, SetUpException {
        
        // reset
        analysisResult = null;
        
        LOGGER.logInfo("Running for defect: " + defect);
        
        Properties props = new Properties();
        props.load(new FileReader(new File("res/metric_base.properties")));
        
        String file = defect.getPath() + defect.getFile();
        
        // check if the file even exists
        if (!new File(sourceTree, file).isFile()) {
            LOGGER.logWarning("File " + file + " doesn't exist in checked out tree",  "Skipping single function metrics");
            return null;
        }
        
        props.setProperty("source_tree", sourceTree.getAbsolutePath());
        props.setProperty("analysis.class", AllLineFilterableFunctionMetrics.class.getCanonicalName());
        props.setProperty("code.extractor.files", file);
        props.setProperty("analysis.code_function.line", String.valueOf(defect.getLine()));
        
        AllLineFilterableFunctionMetrics.setAddObservable(true);
        ObservableAnalysis.setObservers(this);
        
        return runKernelHaven(props);
    }
    
    private List<MultiMetricResult> runKernelHaven(Properties props) throws SetUpException {
        Timestamp.INSTANCE.setToNow(); // update Timestamp so result file will have different name
        
        Configuration config = new Configuration(props);
        DefaultSettings.registerAllSettings(config);
        
        LOGGER.logInfo("Running KernelHaven");
//        Level level = KbuildCrawler.DISABLE_KH_LOGGING ? Level.NONE : Level.WARNING;
        LOGGER.setLevel(Level.INFO);
        
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
        
        // clear kh/output directory
        File outputDir = new File("kh/output/");
        try {
            Util.deleteFolder(outputDir);
        } catch (IOException e) {
            LOGGER.logException("Can't delete output dir", e);
        }
        outputDir.mkdir();
        
        return analysisResult;
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
