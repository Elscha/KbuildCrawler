package net.ssehub.kBuildCrawler;

import java.io.File;
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
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;
import net.ssehub.kernel_haven.util.Logger;

public class KbuildCrawler {
    public final static File TESTDATA = new File("testdata");

    public static void main(String[] args) throws Exception {
        if (Logger.get() == null) {
            Logger.init();
        }
        
        List<FailureTrace> failures = readMails(new File(TESTDATA, "2016-August.txt.gz"));
        runMetrics(new File("gitTest"), failures);
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
    
    private static void runMetrics(File gitFolder, List<FailureTrace> failures) throws GitException {
        GitInterface git = new GitInterface(gitFolder);
        KernelHavenRunner runner = new KernelHavenRunner();
        
        for (FailureTrace failureTrace : failures) {
            List<MultiMetricResult> result = runner.run(git, failureTrace);
        
            // TODO: just print the result out for now
            System.out.println();
            System.out.println();
            System.out.println("Result for failure trace:");
            System.out.println(failureTrace);
            System.out.println();
           
            if (result != null) {
                for (MultiMetricResult mr : result) {
                    System.out.println(mr);
                }
            } else {
                System.out.println("null");
            }
        }
    }

}
