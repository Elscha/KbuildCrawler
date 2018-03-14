package net.ssehub.kBuildCrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.ssehub.kBuildCrawler.git.GitException;
import net.ssehub.kBuildCrawler.git.GitInterface;
import net.ssehub.kBuildCrawler.git.mail_parsing.FailureTrace;
import net.ssehub.kBuildCrawler.git.mail_parsing.GitUtils;
import net.ssehub.kBuildCrawler.mail.IMailSource;
import net.ssehub.kBuildCrawler.mail.Mail;
import net.ssehub.kBuildCrawler.mail.MailParser;
import net.ssehub.kBuildCrawler.mail.MailUtils;
import net.ssehub.kBuildCrawler.mail.ZipMailSource;
import net.ssehub.kBuildCrawler.metrics.KernelHavenRunner;
import net.ssehub.kernel_haven.io.excel.ExcelBook;
import net.ssehub.kernel_haven.io.excel.ExcelSheetWriter;
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;
import net.ssehub.kernel_haven.util.Timestamp;

public class KbuildCrawler {
    public final static File TESTDATA = new File("testdata");
    private static final File TEST_ARCHIVE = new File(TESTDATA, "2016-August.txt.gz");
    
    public static final boolean DISABLE_KH_LOGGING = true;

    public static void main(String[] args) throws Exception {
        FileOutputStream out = new FileOutputStream(Timestamp.INSTANCE.getFilename("MailCrawler", "log"));
        Logger.get().addTarget(out);
        Logger.get().setLevel(Level.DEBUG);
        
        File gitRepo = new File("gitTest");
        
        File[] archives = null;
        if (args.length >= 1) {
            String[] fileArguments = args[0].split(":");
            archives = new File[fileArguments.length];
            for (int i = 0; i < fileArguments.length; i++) {
                archives[i] = new File(fileArguments[i]);
                
                if (!archives[i].isFile()) {
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
    
    private static void runMetrics(File gitFolder, List<FailureTrace> failures) throws GitException, IOException, FormatException {
        GitInterface git = new GitInterface(gitFolder);
        KernelHavenRunner runner = new KernelHavenRunner();
        
        String[] newHeader = null;
        int step = 0;
        
        try (ExcelBook output = new ExcelBook(new File(Timestamp.INSTANCE.getFilename("MetricsResult", "xlsx")))) {
            try (ExcelSheetWriter writer = output.getWriter("Result")) {
        
//                List<MultiMetricResult> aggregatedResults = new LinkedList<>();
                for (FailureTrace failureTrace : failures) {
                    System.err.println("Processing " + ++step + " of " + failures.size());
                    String gitInfo;
                    if (failureTrace.getGitInfo().getCommit() != null) {
                        gitInfo = failureTrace.getGitInfo().getCommit().substring(0, 8);
                    } else if (failureTrace.getGitInfo().getBranch() != null) {
                        gitInfo = failureTrace.getGitInfo().getBranch();
                    } else {
                        gitInfo = "(unknown)";
                    }
                    
                    String name = failureTrace.getFormattedDate(false) + " " + gitInfo;
                    
                    List<MultiMetricResult> result = runner.run(git, failureTrace);
        
                    if (result != null && !result.isEmpty()) {
                        Logger.get().logInfo("Got result for " + name);
                        for (MultiMetricResult multiMetricResult : result) {
                            int oldLength = multiMetricResult.getHeader().length;
                            if (null == newHeader) {
                                newHeader = new String[oldLength + 3];
                                newHeader[0] = "Date";
                                newHeader[1] = "Repository";
                                newHeader[2] = "Commit";
                                System.arraycopy(multiMetricResult.getHeader(), 0, newHeader, 3, oldLength);
                            }
                            
                            Object[] newValues = new Object[oldLength + 3];
                            newValues[0] = failureTrace.getMail().getDate();
                            newValues[1] = failureTrace.getGitInfo().getBase();
                            newValues[2] = failureTrace.getGitInfo().getCommit();
                            if (null == newValues[2]) {
                                newValues[2] = failureTrace.getGitInfo().getBranch();
                            }
                            System.arraycopy(multiMetricResult.getContent(), 0, newValues, 3, oldLength);
                            

                            writer.writeObject(new MultiMetricResult(newHeader, newValues));
                            writer.flush();
                        }
                        
                    } else {
                        Logger.get().logInfo("Got NO result for " + name);
                    }
                }
            }
        
        }
    }

}
