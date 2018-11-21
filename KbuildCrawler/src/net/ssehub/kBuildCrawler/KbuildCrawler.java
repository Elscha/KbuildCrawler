package net.ssehub.kBuildCrawler;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.ssehub.kBuildCrawler.git.GitException;
import net.ssehub.kBuildCrawler.git.GitInterface;
import net.ssehub.kBuildCrawler.git.mail_parsing.FailureTrace;
import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;
import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect.Type;
import net.ssehub.kBuildCrawler.git.mail_parsing.GitData;
import net.ssehub.kBuildCrawler.git.mail_parsing.GitUtils;
import net.ssehub.kBuildCrawler.mail.IMailSource;
import net.ssehub.kBuildCrawler.mail.Mail;
import net.ssehub.kBuildCrawler.mail.MailParser;
import net.ssehub.kBuildCrawler.mail.MailUtils;
import net.ssehub.kBuildCrawler.mail.ZipMailSource;
import net.ssehub.kBuildCrawler.metrics.AbstractKernelHavenRunner;
import net.ssehub.kBuildCrawler.metrics.KernelHavenProcessRunner;
import net.ssehub.kernel_haven.io.excel.ExcelBook;
import net.ssehub.kernel_haven.io.excel.ExcelSheetWriter;
import net.ssehub.kernel_haven.metric_haven.multi_results.MeasuredItem;
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;
import net.ssehub.kernel_haven.util.Timestamp;

public class KbuildCrawler {
    public final static File TESTDATA = new File("testdata");
    private static final File TEST_ARCHIVE = new File(TESTDATA, "2016-August.txt.gz");
    
    private static final int DEBUG_PROCESS_ONLY = -1;
    
    public static final boolean DISABLE_KH_LOGGING = true;

    public static void main(String[] args) throws Exception {
        FileOutputStream out = new FileOutputStream(Timestamp.INSTANCE.getFilename("MailCrawler", "log"));
        Logger.get().clearAllTargets();
        Logger.get().addTarget(out);
        Logger.get().setLevel(Level.DEBUG);
        
        File gitRepo = new File("gitTest");
        
        File[] archives = null;
        if (args.length >= 1) {
            System.err.println("Mailinglist items: " + args[0]);
            String[] fileArguments = args[0].split(":");
            archives = new File[fileArguments.length];
            for (int i = 0; i < fileArguments.length; i++) {
                archives[i] = new File(fileArguments[i]);
                
                if (!archives[i].isFile()) {
                    System.err.println(archives[i] + " is not a valid file");
                    Logger.get().logError(archives[i] + " is not a valid file");
                }
            }
        }
        
        if (archives == null || archives.length == 0) {
            archives = new File[] {TEST_ARCHIVE};
            if (!TEST_ARCHIVE.isFile()) {
                Logger.get().logError(TEST_ARCHIVE + " is not a valid file");
                System.exit(0);
            }
        }
        
        if (args.length >= 2) {
            gitRepo = new File(args[1]);
        }
        
        List<FailureTrace> failures = new LinkedList<FailureTrace>();
        for (int i = 0; i < archives.length; i++) {
            failures.addAll(readMails(archives[i]));
        }
        runMetrics(gitRepo, failures);
        
        out.close();
    }

    private static List<FailureTrace> readMails(File zipFile) throws Exception {
        IMailSource augMails = new ZipMailSource(zipFile);
        MailParser parser = new MailParser();
        
        // Aug 2016 mails
        List<Mail> mails = parser.loadMails(augMails);
        
        // Only mails from Kbuild test robot, containing compilation problems
        mails = MailUtils.filterForKbuildTestRobot(mails, true);
        mails = MailUtils.filterForCompilationProblems(mails, false);
        
        // Extract needed infos:
        List<FailureTrace> failures = GitUtils.convertToTraces(mails);
        
        return failures;
    }
    
    @SuppressWarnings("unused") // for the DEBUG_PROCESS_ONLY flag
    private static void runMetrics(File gitFolder, List<FailureTrace> failures) throws GitException, IOException {
        GitInterface git = new GitInterface(gitFolder);
        AbstractKernelHavenRunner runner = new KernelHavenProcessRunner();
        
        int failureIndex = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss (dd.MM.yy)");
        String[] previousMetrics = null;
        Boolean previousConsideredIncludedFile = null;
        
        try (ExcelBook output = new ExcelBook(new File(Timestamp.INSTANCE.getFilename("MetricsResult", "xlsx")))) {
            try (ExcelSheetWriter writer = output.getWriter("Result")) {
        
                for (FailureTrace failureTrace : failures) {
                    if (DEBUG_PROCESS_ONLY > 0 && failureIndex >= DEBUG_PROCESS_ONLY) {
                        System.err.println("Stopping early, because DEBUG_PROCESS_ONLY is set to " + DEBUG_PROCESS_ONLY);
                        break;
                    }
                    System.err.println("Processing " + ++failureIndex + " of " + failures.size() + " at "
                        + sdf.format(Calendar.getInstance().getTime()));
                    String gitInfo;
                    if (failureTrace.getGitInfo().getCommit() != null) {
                        gitInfo = failureTrace.getGitInfo().getCommit().substring(0, 8);
                    } else if (failureTrace.getGitInfo().getBranch() != null) {
                        gitInfo = failureTrace.getGitInfo().getBranch();
                    } else if (failureTrace.getGitInfo().is0DayCommit()) {
                        gitInfo = GitData.ZERO_DAY_GIT_URL + " -> " + failureTrace.getGitInfo().get0DayBranch();
                    } else {
                        gitInfo = "(unknown)";
                    }
                    
                    String name = failureTrace.getFormattedDate(false) + " " + gitInfo;
                    
                    long t0 = System.currentTimeMillis();
                    List<MultiMetricResult> result = runner.run(git, failureTrace);        
                    if (result != null && !result.isEmpty()) {
                        Logger.get().logInfo("Got result for " + name);
                        
                        List<Type> types = determineTypes(result, failureTrace.getDefects());
                        int resultIndex = -1;
                        for (MultiMetricResult multiMetricResult : result) {
                            resultIndex++;
                            
                            int oldLength = multiMetricResult.getHeader().length;
                            
                            if (previousMetrics == null) {
                                previousMetrics = multiMetricResult.getMetrics();
                                previousConsideredIncludedFile = multiMetricResult.getMeasuredItem().isConsiderIncludedFile();
                                
                                // write excel header
                                String[] newHeader = new String[oldLength + 4];
                                newHeader[0] = "Date";
                                newHeader[1] = "Repository";
                                newHeader[2] = "Commit";
                                newHeader[3] = "Type";
                                System.arraycopy(multiMetricResult.getHeader(), 0, newHeader, 4, oldLength);
                                writer.writeHeader((Object[]) newHeader);
                            }
                            
                            try {
                                multiMetricResult = AbstractKernelHavenRunner.tryFixToFormat(previousMetrics, multiMetricResult);
                                oldLength = multiMetricResult.getHeader().length;
                            } catch (IllegalArgumentException e) {
                                System.err.println("Error! Can't adapt MultiMetricResult to expected format");
                                System.err.println("Expected Metrics: " + Arrays.toString(previousMetrics));
                                System.err.println("Actual Metrics:   " + Arrays.toString(multiMetricResult.getMetrics()));
                                System.err.println("Skipping this line");
                                continue;
                            }
                            
                            if (multiMetricResult.getMeasuredItem().isConsiderIncludedFile() != previousConsideredIncludedFile) {
                                System.err.println("Warning: Found element with isConsiderIncludedFile = "
                                        + multiMetricResult.getMeasuredItem().isConsiderIncludedFile() + ", but expected "
                                        + previousConsideredIncludedFile);
                                System.err.println("Forcibly setting value to " + previousConsideredIncludedFile
                                        + " to match header (potential loss of information)");
                                multiMetricResult.getMeasuredItem().setConsiderIncludedFile(notNull(previousConsideredIncludedFile));
                            }
                            
                            Object[] newValues = new Object[oldLength + 4];
                            newValues[0] = failureTrace.getMail().getDate();
                            newValues[1] = failureTrace.getGitInfo().getBase();
                            newValues[2] = failureTrace.getGitInfo().getCommit();
                            if (null == newValues[2]) {
                                newValues[2] = failureTrace.getGitInfo().getBranch();
                            }
                            newValues[3] = types.get(resultIndex).name();
                            System.arraycopy(multiMetricResult.getContent(), 0, newValues, 4, oldLength);
                            
                            writer.writeRow(newValues);
                            writer.flush();
                        }
                        
                    } else {
                        Logger.get().logInfo("Got NO result for " + name);
                    }
                    
                    long duration = System.currentTimeMillis() - t0;
                    System.err.println("Handling (+writing) KH output took " + duration + "ms");
                }
            }
        
        }
    }
    
    /**
     * Package visiblity for test cases.
     */
    static List<Type> determineTypes(List<MultiMetricResult> result, List<FileDefect> defects) {
        List<Type> types = new ArrayList<>(result.size());
        
        // "sort" result by file
        Map<File, List<MeasuredItem>> resultByFileMap = new HashMap<>();
        for (MultiMetricResult mmi: result) {
            File file = new File(mmi.getMeasuredItem().getMainFile());
            resultByFileMap.putIfAbsent(file, new LinkedList<>());
            resultByFileMap.get(file).add(mmi.getMeasuredItem());
        }
        
        for (MultiMetricResult mmr : result) {
            
            int nextMiLineNumber = Integer.MAX_VALUE;
            
            // find the line number of the next result item after the current item
            File file = new File(mmr.getMeasuredItem().getMainFile());
            for (MeasuredItem otherMi : resultByFileMap.get(file)) {
                // the line number of the next item has to be > than the current line number
                // and we want to find the lowest next line number possible
                if (otherMi.getLineNo() > mmr.getMeasuredItem().getLineNo() && otherMi.getLineNo() < nextMiLineNumber) {
                    nextMiLineNumber = otherMi.getLineNo();
                }
            }
            
            
            types.add(determineType(mmr, defects, nextMiLineNumber));
        }
        
        return types;
    }

    private static Type determineType(MultiMetricResult mmr, List<FileDefect> defects, int nextMiLineNumber) {
        Type result = Type.UNKNOWN;
        
        MeasuredItem mi = mmr.getMeasuredItem();
        File miFile = new File(mi.getMainFile());
        
        for (FileDefect defect : defects) {
            File defectFile = new File(defect.getPath() + defect.getFile());
            
            // check if file is the same and measured line number is <= defect line (that means, it is possible that
            // defect lies within the function described by measured item)
            // also check that the defect line is not >= the line number of the next measured item (that would mean that
            // the defect is inside the next function)
            if (miFile.equals(defectFile) && mi.getLineNo() <= defect.getLine() && defect.getLine() < nextMiLineNumber) {
                
                if (defect.getType().ordinal() < result.ordinal()) {
                    result = defect.getType();
                }
            }
        }
        
        return result;
    }

}
