package net.ssehub.kBuildCrawler;


import java.io.File;
import java.util.List;

import net.ssehub.kBuildCrawler.git.FailureTrace;
import net.ssehub.kBuildCrawler.git.GitUtils;
import net.ssehub.kBuildCrawler.git.plugins.GitCmdPlugin;
import net.ssehub.kBuildCrawler.git.plugins.MultiRepositoryPlugin;
import net.ssehub.kBuildCrawler.mail.IMailSource;
import net.ssehub.kBuildCrawler.mail.Mail;
import net.ssehub.kBuildCrawler.mail.MailParser;
import net.ssehub.kBuildCrawler.mail.MailUtils;
import net.ssehub.kBuildCrawler.mail.ZipMailSource;

@SuppressWarnings("unused")
public class KbuildCrawler {
    public final static File TESTDATA = new File("testdata");

    public static void main(String[] args) throws Exception {
        //readMails();
        //gitCheckoutAndDiff();
        //gitCheckoutAndFetch();
        //downloadOwnRepositoryAndDiff();
        //downloadAllAugReports();
        
        File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
        tmpFolder = new File(tmpFolder, "gitTest");
//        GitCmdPlugin cmdPlugin = new GitCmdPlugin(null);
//        cmdPlugin.setBasePath(new File(tmpFolder, "Linux"));
//        String remote = cmdPlugin.getRemoteURL();
//        System.out.println(remote);
        MultiRepositoryPlugin multiRepos = new MultiRepositoryPlugin(tmpFolder);
        multiRepos.clone("https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git", "master");
        
//        multiRepos.clone("https://github.com/QualiMaster/QM-IConf.git", "permissibleParameters");
//        multiRepos.clone("https://github.com/QualiMaster/Infrastructure.git", null);
//        String diff = multiRepos.diff("30e8ad93ee22eba033c8200cc0df8060ec3d06f8",
//            "e899d50c52c07078efca7cec30ce78aa09860e22");
//        System.out.println(diff);
    }

    private static void downloadAllAugReports() throws Exception {
        File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
        tmpFolder = new File(tmpFolder, "gitTest");
        MultiRepositoryPlugin multiRepos = new MultiRepositoryPlugin(tmpFolder);
        
        File zipFile = new File(TESTDATA, "2016-August.txt.gz");
        IMailSource augMails = new ZipMailSource(zipFile);
        MailParser parser = new MailParser();
        
        // Aug 2016 mails
        List<Mail> mails = parser.loadMails(augMails);
        
        // Only mails from Kbuild test robot, containing compilation problems
        mails = MailUtils.filterForKbuildTestRobot(mails, true);
        mails = MailUtils.filterForCompilationProblems(mails, false);
        
        // Extract needed infos:
        List<FailureTrace> failures = GitUtils.convertToTraces(mails);
        
        for (FailureTrace failureTrace : failures) {
            multiRepos.restoreCommit(failureTrace.getGitInfo());
        }
    }

    private static void downloadOwnRepositoryAndDiff() {
        File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
        tmpFolder = new File(tmpFolder, "gitTest");
        MultiRepositoryPlugin multiRepos = new MultiRepositoryPlugin(tmpFolder);
        multiRepos.clone("https://github.com/Elscha/KbuildCrawler.git", null);
//        String diff = multiRepos.diff("d9c0fc70ad50f17a7c41ac1ce31deec195ab368b",
//            "29cd71d2d360c5cc214fd7bfdf084168645cde9f");
        String diff = multiRepos.diff("29cd71d2d360c5cc214fd7bfdf084168645cde9f",
                "4726ee9ca0e27a0cfe666e2eac7e0b77d2d1119a");
        System.out.println(diff);
    }
    
    private static void gitCheckoutAndFetch() {
        GitCmdPlugin git = new GitCmdPlugin(null);
        File gitRepo = new File(System.getProperty("java.io.tmpdir"));
        gitRepo = new File(gitRepo, "gitTest/QM-IConf");
        git.setBasePath(gitRepo);
        System.out.println(git.checkout("d4a43f58486d49acd4f304c39eb668340fa8397a"));
        git.fetch();
        System.out.println(git.swithToBranch("permissibleParameters"));
    }
    
    private static void gitCheckoutAndDiff() {
        GitCmdPlugin git = new GitCmdPlugin(null);
        File tmpFolder = new File(System.getProperty("java.io.tmpdir"));
        tmpFolder = new File(tmpFolder, "gitTest");
        System.out.println(tmpFolder.getAbsolutePath());
        //tmpFolder.deleteOnExit();
        git.setBasePath(tmpFolder);
        File f = git.clone("https://github.com/QualiMaster/QM-IConf.git", "permissibleParameters");
//        File f = git.clone("https://git.kernel.org/pub/scm/linux/kernel/git/groeck/linux-staging.git", "hwmon-next");
        System.out.println(f);
        git.setBasePath(f);
        System.out.println(git.diff("ccb91aa2bec3d36dfb72a566cb39c6807cb68f99", "d4a43f58486d49acd4f304c39eb668340fa8397a"));
    }

    private static void readMails() throws Exception {
        File zipFile = new File(TESTDATA, "2016-August.txt.gz");
        IMailSource augMails = new ZipMailSource(zipFile);
        MailParser parser = new MailParser();
        
        // Aug 2016 mails
        List<Mail> mails = parser.loadMails(augMails);
        
        // Only mails from Kbuild test robot, containing compilation problems
        mails = MailUtils.filterForKbuildTestRobot(mails, true);
        mails = MailUtils.filterForCompilationProblems(mails, false);
        
        // Extract needed infos:
        List<FailureTrace> failures = GitUtils.convertToTraces(mails);
        System.out.println(failures.toString());
    }

}
