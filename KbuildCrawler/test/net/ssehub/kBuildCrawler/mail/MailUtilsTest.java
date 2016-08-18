package net.ssehub.kBuildCrawler.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kBuildCrawler.AllTests;
import net.ssehub.kBuildCrawler.mail.IMailSource;
import net.ssehub.kBuildCrawler.mail.Mail;
import net.ssehub.kBuildCrawler.mail.MailUtils;
import net.ssehub.kBuildCrawler.mail.ZipMailSource;

/**
 * Tests the {@link MailUtils}.
 * @author El-Sharkawy
 *
 */
public class MailUtilsTest {
    /**
     * Test data set contains:
     * <ul>
     *   <li>37 mails from lkp at intel.com address, but</li>
     *   <ul>
     *     <li>36 are from KBuild test robot</li>
     *     <li>1 is from its developer: Fengguang Wu</li>
     *   </ul>
     *   <li>76 mails from fengguang.wu at intel.com address, but</li>
     *   <ul>
     *     <li>75 are from KBuild test robot</li>
     *     <li>1 is from its developer: Fengguang Wu</li>
     *   </ul>
     * </ul>
     */
    static final int EXPECTED_NO_OF_ROBOT_MAILS = 36 + 75;
    
    /**
     * Tests the correct splitting of mails into mails from Kbuild test robot and others.
     * @throws Exception Must not occur.
     */
    @Test
    public void testFilterForKbuildTestRobot() throws Exception {
        // Already tested in separate test
        File zipFile = new File(AllTests.TESTDATA, "2016-August.txt.gz");
        IMailSource augMails = new ZipMailSource(zipFile);
        List<Mail> mails = augMails.loadMails();
        
        List<Mail> robotMails = MailUtils.filterForKbuildTestRobot(mails, true);
        List<Mail> otherMails = MailUtils.filterForKbuildTestRobot(mails, false);
        
        Assert.assertNotNull(robotMails);
        Assert.assertNotNull(otherMails);
        Assert.assertEquals(ZipMailSourceTests.EXPECTED_MAILS_SIZE, robotMails.size() + otherMails.size());
        Assert.assertEquals(EXPECTED_NO_OF_ROBOT_MAILS, robotMails.size());
    }
    
    /**
     * Tests the {@link MailUtils#hasCompilerProblem(Mail)} method.
     */
    @Test
    public void testHasCompilerProblem() {
        Mail linkerError = new Mail("someone", "today", "linker", "Some text\n>> ERROR: \".dma_buf_map_attachment\" "
            + "[drivers/infiniband/core/ib_core.ko] undefined!\n more text");
        Mail compilerProblem = new Mail("someone", "today", "compiler problem", "Some text\n>>>> "
            + "drivers/hwmon/xgene-hwmon.c:38:22: fatal error: acpi/pcc.h: No such file or directory\n more text");
        Mail compilerError = new Mail("someone", "today", "compiler error", "Some text\n>>>> "
            + "drivers/hwmon/xgene-hwmon.c:38:22: fatal error: acpi/pcc.h: No such file or directory\n   compilation "
            + "terminated.\n more text");
        Assert.assertFalse(MailUtils.hasCompilerProblem(linkerError));
        Assert.assertTrue(MailUtils.hasCompilerProblem(compilerProblem));
        Assert.assertTrue(MailUtils.hasCompilerProblem(compilerError));
    }
    
    /**
     * Tests the {@link MailUtils#filterForCompilationProblems(List, boolean)} method.
     */
    @Test
    public void testFilterForCompilationProblems() {
        List<Mail> mails = new ArrayList<>();
        mails.add(new Mail("someone", "today", "linker", "Some text\n>> ERROR: \".dma_buf_map_attachment\" "
            + "[drivers/infiniband/core/ib_core.ko] undefined!\n more text"));
        mails.add(new Mail("someone", "today", "compiler problem", "Some text\n>>>> "
            + "drivers/hwmon/xgene-hwmon.c:38:22: fatal error: acpi/pcc.h: No such file or directory\n more text"));
        mails.add(new Mail("someone", "today", "compiler error", "Some text\n>>>> "
            + "drivers/hwmon/xgene-hwmon.c:38:22: fatal error: acpi/pcc.h: No such file or directory\n   compilation "
            + "terminated.\n more text"));
        
        List<Mail> problems = MailUtils.filterForCompilationProblems(mails, false);        
        Assert.assertNotNull(problems);
        Assert.assertEquals(2, problems.size());
        Assert.assertFalse(problems.contains(mails.get(0)));
        
        List<Mail> errors = MailUtils.filterForCompilationProblems(mails, true);
        Assert.assertNotNull(errors);
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.contains(mails.get(2)));
    }
}
