package net.ssehub.kBuildCrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static void main(String[] args) throws Exception {
        if (Logger.get() == null) {
            Logger.init(new FileOutputStream(Timestamp.INSTANCE.getFilename("MailCrawler", "log")));
            Logger.get().setLevel(Level.DEBUG);
        }
        
        File mailZip = new File(TESTDATA, "2016-August.txt.gz");
        File gitRepo = new File("gitTest");
        
        if (args.length >= 1) {
            mailZip = new File(args[0]);
        }
        if (!mailZip.isFile()) {
            Logger.get().logError(mailZip + " is not a valid file");
        }
        
        if (args.length >= 2) {
            gitRepo = new File(args[1]);
        }
        
        List<FailureTrace> failures = readMails(mailZip);
        runMetrics(gitRepo, failures);
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
        
        try (ExcelBook output = new ExcelBook(new File(Timestamp.INSTANCE.getFilename("MetricsResult", "xlsx")))) {
        
            for (FailureTrace failureTrace : failures) {
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
                    
                    try (ExcelSheetWriter writer = output.getWriter(name)) {
                        for (MultiMetricResult mr : result) {
                            writer.writeObject(mr);
                        }
                    }
                    
                } else {
                    Logger.get().logInfo("Got NO result for " + name);
                }
            }
        }
    }

}
