package net.ssehub.kBuildCrawler.metrics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.metric_haven.metric_components.BlocksPerFunctionMetric;
import net.ssehub.kernel_haven.metric_haven.metric_components.CyclomaticComplexityMetric;
import net.ssehub.kernel_haven.metric_haven.metric_components.DLoC;
import net.ssehub.kernel_haven.metric_haven.metric_components.FanInOutMetric;
import net.ssehub.kernel_haven.metric_haven.metric_components.NestingDepthMetric;
import net.ssehub.kernel_haven.metric_haven.metric_components.VariablesPerFunctionMetric;
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.io.ITableCollection;
import net.ssehub.kernel_haven.util.io.ITableReader;
import net.ssehub.kernel_haven.util.io.TableCollectionReaderFactory;

/**
 * Alternative implementation of {@link KernelHavenRunner}, which runs KernelHaven in a separate process.
 * @author El-Sharkawy
 *
 */
public class KernelHavenProcessRunner extends AbstractKernelHavenRunner {
    
    private static final String KH_DIR = "kh";
    private static final String BASE_CONFIGURATION = "res/single_metric.properties";
    private static final Class<?>[] METRICS = {BlocksPerFunctionMetric.class, CyclomaticComplexityMetric.class,
        DLoC.class, FanInOutMetric.class, NestingDepthMetric.class, VariablesPerFunctionMetric.class};

    @Override
    protected List<MultiMetricResult> runNonFilterableMetrics(File sourceTree) throws IOException, SetUpException {
        List<MultiMetricResult> results = new ArrayList<>();
        
        // Iterate over all metric (execute them in independent processes)
        long t0 = System.currentTimeMillis();
        for (Class<?> metric : METRICS) {
            String analysisName = metric.getSimpleName() + " on " + sourceTree.getName();
            File configFile = prepareConfiguration(sourceTree, metric);
            
            // Execute the process, keep track of std out stream (we log every thing to console)
            OutputStream outStream = new ByteArrayOutputStream();
            OutputStream errStream = new ByteArrayOutputStream();
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-Xmx300G", "-Xmx300G", "-jar",
                "KernelHaven_withsource.jar", configFile.getAbsolutePath());
            processBuilder.directory(new File(KH_DIR));
            boolean success = Util.executeProcess(processBuilder, "MetricsRunner: " + analysisName, outStream,
                errStream, 0);

            if (success) {
                List<Path> list = new ArrayList<>();
                Files.newDirectoryStream(Paths.get(KH_DIR, "output"), p -> p.toFile().getName().endsWith(".csv"))
                    .forEach(list::add);
                System.err.println(list.size() + " files produced for " + analysisName + ", start merging them");
                
                for (Path path : list) {
                    File file = path.toFile();
                    try (ITableCollection csvCollection = TableCollectionReaderFactory.INSTANCE.openFile(file)) {
                        String firstAndOnlyTable = csvCollection.getTableNames().iterator().next();
                        try (ITableReader reader = csvCollection.getReader(firstAndOnlyTable)) {
                            String[][] fullContent = reader.readFull();
                            // TODO SE: merge results
                        }
                    }
                    
                    //TODO SE delete temporarily created files with: file.delete();
                }
                
            } else {
                LOGGER.logError2("Could not execute ", analysisName, ", cause: " + outStream.toString());
            }
            
            configFile.delete();
        }
        long delta = System.currentTimeMillis() - t0;
        String elapsedTime = (new SimpleDateFormat("HH:mm:ss")).format(new Date(delta));
        System.err.println(METRICS.length + " metric analyses (+merging results) took" + elapsedTime);
        
        return results;
    }

    @Override
    protected List<MultiMetricResult> runLineFilteredMetrics(File sourceTree, FileDefect defect) throws IOException, SetUpException {
        // TODO Auto-generated method stub
        return new ArrayList<MultiMetricResult>();
    }
    
    private File prepareConfiguration(File sourceTree, Class<?> metricClass) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileReader(new File(BASE_CONFIGURATION)));
        
        props.setProperty("source_tree", sourceTree.getAbsolutePath());
        props.setProperty("analysis.metrics_runner.metrics_class", metricClass.getCanonicalName());
        
        File tmpProperties = File.createTempFile("SingleMetricAnalysis", "properties");
        tmpProperties.deleteOnExit();
        
        
        try (OutputStream output = new FileOutputStream(tmpProperties)) {
            props.store(output, null);
        }
        
        return tmpProperties;
    }

}
