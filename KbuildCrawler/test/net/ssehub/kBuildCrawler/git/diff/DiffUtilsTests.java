package net.ssehub.kBuildCrawler.git.diff;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kBuildCrawler.io.IOUtils;

/**
 * Tests the {@link DiffUtils}.
 * @author El-Sharkawy
 *
 */
public class DiffUtilsTests {
    
    /**
     * Tests {@link DiffUtils#parseDiff(String)} if only one file was added.
     */
    @Test
    public void testParseFileDiff_FileAdded() {
        String diffString = "diff --git a/Readme.md b/Readme.md\nnew file mode 100644\nindex 0000000..4542d91\n"
            + "--- /dev/null\n+++ b/Readme.md\n@@ -0,0 +1,8 @@\n+# KbuildCrawler\n"
            + "+A crawler ro reproducing failures reported by the Kbuild Test Robot.\n+\n"
            + "+## Implementation status\n+Early implementation, not finished, not working\n+\n"
            + "+## Usage\n"
            + "+- Download mailing list archives from: [kbuild-all mailinglist]"
            + "(https://lists.01.org/pipermail/kbuild-all/)";
        System.out.println(diffString);
        String[] lines = diffString.split(IOUtils.MAIL_LINEFEED_REGEX);
        
        FileDiff diff = DiffUtils.parseFileDiff(lines, 0, lines.length);
        Assert.assertNotNull(diff);
        Assert.assertEquals("Readme.md", diff.getFile());
        Assert.assertEquals(ChangeType.ADDED, diff.getFileChange());
    }

}
