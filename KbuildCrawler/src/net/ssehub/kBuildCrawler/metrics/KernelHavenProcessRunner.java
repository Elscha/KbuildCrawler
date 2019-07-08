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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.metric_haven.metric_components.config.FeatureSizeType;
import net.ssehub.kernel_haven.metric_haven.metric_components.config.MetricSettings;
import net.ssehub.kernel_haven.metric_haven.multi_results.MeasuredItem;
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.io.ITableCollection;
import net.ssehub.kernel_haven.util.io.ITableReader;
import net.ssehub.kernel_haven.util.io.TableCollectionReaderFactory;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Alternative implementation of {@link KernelHavenRunner}, which runs KernelHaven in a separate process.
 * @author El-Sharkawy
 *
 */
public class KernelHavenProcessRunner extends AbstractKernelHavenRunner {
    
    private static final String KH_DIR = "kh";
    private static final String BASE_CONFIGURATION = "res/all_metrics.properties";
    
    /**
     * 10 min time out.
     */
    private static final long KH_TIMEOUT = 10 * 60 * 1000;
    private static final int MAX_TRIES = 1;

    private static final int MAX_GB_FOR_KH = 10;
    private static final String MAX_MEMORY = "-Xmx" + MAX_GB_FOR_KH + "G";
    private static final String INITIAL_MEMORY = "-Xms" + MAX_GB_FOR_KH + "G";
    
    private void readMultiMetricResults(ITableReader reader, List<MultiMetricResult> resultList) throws IOException {
        // Read Header
        final String[] header = reader.readNextRow();
        
        if (header == null || header.length == 0) {
            return;
        }
        
        boolean hasIncludedFile = header[1].equals("Included File");
        @NonNull String[] metrics = new @NonNull String[header.length - (hasIncludedFile ? 4 : 3)];
        System.arraycopy(header, hasIncludedFile ? 4 : 3, metrics, 0, metrics.length);
        
        String[] content = null;
        while ((content = reader.readNextRow()) != null) {
            MeasuredItem mi;
            if (hasIncludedFile) {
                mi = new MeasuredItem(content[0], content[1], Integer.valueOf(content[2]), content[3]);
                mi.setConsiderIncludedFile(true);
            } else {
                mi = new MeasuredItem(content[0], Integer.valueOf(content[1]), content[2]);
                mi.setConsiderIncludedFile(false);
            }

            @Nullable Double[] values = new @Nullable Double[metrics.length];
            int index = 0;
            for (int i =  hasIncludedFile ? 4 : 3; i < content.length; i++) {
                if (!content[i].isEmpty())  {
                    values[index] = Double.valueOf(content[i]);
                }
                
                index++;
            }
            
            MultiMetricResult result = new MultiMetricResult(mi, metrics, values);
            resultList.add(result);
        }
    }
    
    @Override
    protected List<MultiMetricResult> runFeatureSizeMetrics(File sourceTree, List<FileDefect> defects)
        throws IOException, SetUpException {
        
        // Selection of Feature Size metrics only
        Properties featureSizeSettings = new Properties();
        featureSizeSettings.setProperty(MetricSettings.ALL_METRIC_VARIATIONS.getKey(), "false");
        featureSizeSettings.setProperty(MetricSettings.FEATURE_SIZE_MEASURING_SETTING.getKey(),
            FeatureSizeType.POSITIVE_SIZES.name());
        
        // First run: Positive Feature Sizes
        File configFile = prepareConfiguration(sourceTree, defects, featureSizeSettings);
        List<MultiMetricResult> result = runMetricsWithConfig(sourceTree, defects, configFile);
        
        // Second run: Total Feature Sizes
        featureSizeSettings.setProperty(MetricSettings.FEATURE_SIZE_MEASURING_SETTING.getKey(),
            FeatureSizeType.TOTAL_SIZES.name());
        File secondConfigFile = prepareConfiguration(sourceTree, defects, featureSizeSettings);
        List<MultiMetricResult> secondResult = runMetricsWithConfig(sourceTree, defects, secondConfigFile);
        
        // Return joined/merged result list
        return AbstractKernelHavenRunner.joinFullMetricResults(result, secondResult);
    }
    
    @Override
    protected List<MultiMetricResult> runMetrics(File sourceTree, List<FileDefect> defects)
            throws IOException, SetUpException {
        
        File configFile = prepareConfiguration(sourceTree, defects, null);
        return runMetricsWithConfig(sourceTree, defects, configFile);
    }
    
    private List<MultiMetricResult> runMetricsWithConfig(File sourceTree, List<FileDefect> defects, File configFile)
            throws IOException, SetUpException {
        
        List<MultiMetricResult> results = new ArrayList<>();
        
        long t0 = System.currentTimeMillis();
        
        // Execute the process, keep track of std out stream (we log every thing to console)
        boolean success = false;
        int tries = 0;
        String errLog = null;
        do {
            // clear output folder of kernelhaven before starting
            File outputFolder = new File("kh/output");
            Util.clearFolder(outputFolder);
            
            File tempFolder = new File("kh/tmp");
            Util.clearFolder(tempFolder);
            
            long started = System.currentTimeMillis();
            OutputStream outStream = new ByteArrayOutputStream();
            OutputStream errStream = new ByteArrayOutputStream();
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-Djava.io.tmpdir=" + tempFolder.getAbsolutePath(),
                    INITIAL_MEMORY, MAX_MEMORY, "-jar", "KernelHaven.jar", configFile.getAbsolutePath());
            processBuilder.directory(new File(KH_DIR));
            success = Util.executeProcess(processBuilder, "MetricsRunner", outStream,
                errStream, KH_TIMEOUT);
            
            if (!success) {
                long executionTime = System.currentTimeMillis() - started;
                tries++;
                errLog = outStream.toString();
                
                if (executionTime < KH_TIMEOUT) {
                    // KH wasn't aborted through time out, there was something critical: No reason to continue loop
                    break;
                } else {
                    System.err.println("    Timeout occured while running metrics, try again "
                        + (MAX_TRIES - tries) + " times.");
                }
            }
        } while (!success && tries < MAX_TRIES);

        if (success) {
            List<Path> list = new ArrayList<>();
            Files.newDirectoryStream(Paths.get(KH_DIR, "output"), p -> p.toFile().getName().endsWith(".csv"))
                .forEach(list::add);
            Collections.sort(list);
            System.err.println("    " + list.size() + " files produced, start merging them");
            
            /*
             * There should only ever be exactly 1 file, but we join stuff just to be sure.
             */
            
            int fileIndex = 1;
            for (Path path : list) {
                File file = path.toFile();
                System.err.println("    Read file: " + fileIndex++ + " (" + file.length() + " bytes)");
                
                try (ITableCollection csvCollection = TableCollectionReaderFactory.INSTANCE.openFile(file)) {
                    String firstAndOnlyTable = csvCollection.getTableNames().iterator().next();
                    try (ITableReader reader = csvCollection.getReader(firstAndOnlyTable)) {
                        
                        List<MultiMetricResult> newResults = new LinkedList<>();
                        readMultiMetricResults(reader, newResults);
                        results = AbstractKernelHavenRunner.joinFullMetricResults(results, newResults);
                    }
                }
                
                // Delete temporarily created output of KernelHaven
                file.delete();
            }
            
        } else {
            LOGGER.logError2("  Could not execute metrics, cause: " + errLog);
        }
        
      //configFile.delete();
        
        long delta = System.currentTimeMillis() - t0;
        System.err.println("  Metric execution (+ merging " + results.size() + " results) took "
            + Util.formatDurationMs(delta));
        
        return results;
    }

    private File prepareConfiguration(File sourceTree, @Nullable List<FileDefect> defects,
        @Nullable Properties settings) throws FileNotFoundException, IOException {
        
        // Read configuration template
        // Stores properties in a sorted order (for debugging issues only):
        // based on: https://stackoverflow.com/a/17011319
        Properties props = new Properties() {
            /**
             * Generated ID
             */
            private static final long serialVersionUID = -5477130775592634228L;

            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<Object>(super.keySet()));
            }
        };
        props.load(new FileReader(new File(BASE_CONFIGURATION)));
        
        // Adapt configuration based to current checkout and metric to execute
        props.setProperty(DefaultSettings.SOURCE_TREE.getKey(), sourceTree.getAbsolutePath());
        
        // check if all folders in code.extractor.files exist
        String[] folders = props.getProperty(DefaultSettings.CODE_EXTRACTOR_FILES.getKey()).split(",");
        StringBuilder codeFilesSetting = new StringBuilder();
        for (String folder : folders) {
            if (new File(sourceTree, folder).isDirectory()) {
                codeFilesSetting.append(folder).append(",");
            } else {
                LOGGER.logWarning(folder + " doesn't exist (yet) in Linux source tree; omitting from code.extractor.files");
            }
        }
        codeFilesSetting.deleteCharAt(codeFilesSetting.length() - 1); // remove trailing ","
        props.setProperty(DefaultSettings.CODE_EXTRACTOR_FILES.getKey(), codeFilesSetting.toString());
        
        
        StringBuilder filesSetting = new StringBuilder();
        StringBuilder linesSetting = new StringBuilder();
        
        for (FileDefect defect : defects) {
            filesSetting.append(defect.getPath()).append(defect.getFile()).append(", ");
            linesSetting.append(defect.getPath()).append(defect.getFile()).append(":").append(defect.getLine())
            .append(", ");
        }
        filesSetting.replace(filesSetting.length() - 2, filesSetting.length(), ""); // remove trailing ", "
        linesSetting.replace(linesSetting.length() - 2, linesSetting.length(), ""); // remove trailing ", "
        
        // we can't filter code files, since ScatteringDegree and FanInOut need the complete code model
        // props.setProperty("code.extractor.files", filesSetting.toString());
        props.setProperty(MetricSettings.FILTER_BY_FILES.getKey(), filesSetting.toString());
        props.setProperty(MetricSettings.LINE_NUMBER_SETTING.getKey(), linesSetting.toString());
        
        // Add extra settings, if defined
        if (null != settings) {
            props.putAll(settings);
        }
        
        // Save temporary configuration and return file (required as a parameter, later)
        File tmpProperties = File.createTempFile("SingleMetricAnalysis", ".properties");
        tmpProperties.deleteOnExit();
        try (OutputStream output = new FileOutputStream(tmpProperties)) {
            props.store(output, null);
        }
        
        return tmpProperties;
    }
    
}
