package net.ssehub.kBuildCrawler.mail;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kBuildCrawler.AllTests;

/**
 * Tests the {@link GZFolderMailSource}, depends on {@link ZipMailSourceTests} and should come afterwards.
 * @author El-Sharkawy
 *
 */
public class GZFolderMailSourceTest {
    
    /**
     * Tests parsing of Mails out of a multiple GZ files.
     * @throws Exception Must not happen!
     */
    @Test
    public void testLoadMails() throws Exception {
        // For comparison
        List<Mail> mails = ZipMailSourceTests.loadAugMails();
        
        // Test: GZFolderMailSource
        IMailSource augMailsByFolder = new GZFolderMailSource(AllTests.TESTDATA);
        List<Mail> doubledMails = augMailsByFolder.loadMails();
        
        Assert.assertNotNull(doubledMails);
        Assert.assertEquals(2 * mails.size(), doubledMails.size());
    }

}
