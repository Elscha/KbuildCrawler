package net.ssehub.kBuildCrawler.git.mail_parsing;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kBuildCrawler.mail.Mail;
import net.ssehub.kBuildCrawler.mail.MailUtils;
import net.ssehub.kBuildCrawler.mail.ZipMailSourceTests;

/**
 * Tests for the {@link GitUtils}.
 * @author El-Sharkawy
 *
 */
public class GitUtilsTest {

    @Test
    public void testConfigRegEx() {
        String configURL = "URL: <http://lists.01.org/pipermail/kbuild-all/attachments/20160801/6273dda8/"
            + "attachment-0001.obj>";
        Assert.assertTrue(configURL.matches(GitUtils.CONFIG_REGEX));
    }
    
    @Test
    public void testDefectRegEx() {
        String defect = ">> drivers/vfio/mdev/vfio_mpci.c:384:17: warning: cast from pointer to integer of "
            + "different size";
        String defectRange = ">> drivers/vfio/mdev/vfio_mpci.c:384:17-18: warning: cast from pointer to integer of "
            + "different size";
        Assert.assertTrue(defect.matches(GitUtils.DEFECT_REGEX));
        Assert.assertTrue(defectRange.matches(GitUtils.DEFECT_REGEX));
    }
    
    /**
     * Tests whether all Kbuild test robot mails from the sample, which contain compilation problems/errors,
     * can be converted to {@link FailureTrace}s, which enable reproducing the report. 
     */
    @Test
    public void testConvertToTraces() {
        List<Mail> mails = ZipMailSourceTests.loadAugMails();
        mails = MailUtils.filterForKbuildTestRobot(mails, true);
        mails = MailUtils.filterForCompilationProblems(mails, false);
        List<FailureTrace> traces = GitUtils.convertToTraces(mails);
        
        // Test
        Assert.assertNotNull(traces);
        Assert.assertEquals(mails.size(), traces.size());
        for (int i = 0, end = traces.size(); i < end; i++) {
            FailureTrace trace = traces.get(i);
            Assert.assertNotNull(trace);
            Assert.assertNotNull(trace.getMail());
            Assert.assertNotNull("Mail \"" + trace.getMail() + "\" has no git data.", trace.getGitInfo());
            Assert.assertNotNull("Mail \"" + trace.getMail() + "\" has no defects.", trace.getDefects());
            Assert.assertFalse("Mail \"" + trace.getMail() + "\" has no defects.", trace.getDefects().isEmpty());
            
            // Test the git data
            GitData gitData =  trace.getGitInfo();
            boolean downloadPossible = gitData.getUrl() != null;
            
            boolean reproducePossible = gitData.cloningPossible() || downloadPossible;
            Assert.assertTrue("Mail \"" + trace.getMail() + "\" cannot be cloned.", reproducePossible);
        }
    }

    /**
     * Tests correct identification of 0Day commits, which require a special handling.
     */
    @Test
    public void testExtractGitData_0Day() {
        String[] lines = {
            "url:    https://github.com/0day-ci/linux/commits/Hoan-Tran/hwmon-xgene-Add-support-for-X-Gene-hwmon-driver/20160725-015356",
            "base:   https://git.kernel.org/pub/scm/linux/kernel/git/groeck/linux-staging.git hwmon-next",
            "config: arm64-allmodconfig (attached as .config)",
            "compiler: aarch64-linux-gnu-gcc (Debian 5.4.0-6) 5.4.0 20160609",
        };
        
        GitData data = GitUtils.extractGitSettings(lines);
        Assert.assertTrue(data.is0DayCommit());
        Assert.assertEquals("Hoan-Tran/hwmon-xgene-Add-support-for-X-Gene-hwmon-driver/20160725-015356",
            data.get0DayBranch());
    }
}
