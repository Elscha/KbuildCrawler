package net.ssehub.kBuildCrawler.mail;
import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kBuildCrawler.AllTests;
import net.ssehub.kBuildCrawler.mail.IMailSource;
import net.ssehub.kBuildCrawler.mail.Mail;
import net.ssehub.kBuildCrawler.mail.ZipMailSource;

/**
 * Tests for the {@link ZipMailSource}.
 * @author El-Sharkawy
 *
 */
public class ZipMailSourceTests {
    static final int EXPECTED_MAILS_SIZE = 135;
    
    /**
     * Loads the sample file via the {@link ZipMailSource}.
     * @return Mails of the sample file, will not be <tt>null</tt>.
     */
    public static List<Mail> loadAugMails() {
        File zipFile = new File(AllTests.TESTDATA, "2016-August.txt.gz");
        IMailSource augMails = new ZipMailSource(zipFile);
        List<Mail> mails = null;
        try {
            mails = augMails.loadMails();
            Assert.assertNotNull(mails);
            Assert.assertEquals(EXPECTED_MAILS_SIZE, mails.size());
        } catch (Exception e) {
            Assert.fail("File \"" + zipFile.getAbsolutePath() + "\"could not be loaded: " + e.getMessage());
        }
        
        return mails;
    }

    /**
     * Tests parsing of Mails out of a GZ file.
     * @throws Exception Must not happen!
     */
    @Test
    public void testLoadMails() throws Exception {
        // Parse test case
        List<Mail> mails = loadAugMails();
        
        Assert.assertNotNull(mails);
        Assert.assertEquals(EXPECTED_MAILS_SIZE, mails.size());
        
        for (int i = 0, end = mails.size(); i < end; i++) {
            Mail mail = mails.get(i);
            Assert.assertNotNull(mail.getFrom());
            Assert.assertNotNull(mail.getDate());
            Assert.assertNotNull(mail.getContent());
            Assert.assertNotNull(mail.getSubject());
        }
    }

}
