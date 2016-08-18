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
        String[] lines = diffString.split(IOUtils.LINEFEED_REGEX);
        
        FileDiff diff = DiffUtils.parseFileDiff(lines, 0, lines.length);
        Assert.assertNotNull(diff);
        Assert.assertEquals("Readme.md", diff.getFile());
        Assert.assertEquals(ChangeType.ADDED, diff.getFileChange());
        Assert.assertEquals(1, diff.getHunks().size());
        
        InFileDiff hunk = diff.getHunks().get(0);
        Assert.assertEquals(0, hunk.getStartLineBefore());
        Assert.assertEquals(0, hunk.getNumberOfChangesLinesBefore());
        Assert.assertEquals(1, hunk.getStartLineAfter());
        Assert.assertEquals(8, hunk.getNumberOfChangesLinesAfter());
    }
    
    /**
     * Tests {@link DiffUtils#parseDiff(String)} if only one file was added.
     */
    @Test
    public void testParseFileDiff_TwoHunksChanged() {
        String diffString = "diff --git a/KbuildCrawler/test/net/ssehub/kBuildCrawler/git/AllGitTests.java\n"
            + "b/KbuildCrawler/test/net/ssehub/kBuildCrawler/git/AllGitTests.java\n"
            + "index 42aecb2..78126a7 100644\n"
            + "--- a/KbuildCrawler/test/net/ssehub/kBuildCrawler/git/AllGitTests.java\n"
            + "+++ b/KbuildCrawler/test/net/ssehub/kBuildCrawler/git/AllGitTests.java\n"
            + "@@ -4,8 +4,12 @@ import org.junit.runner.RunWith;\n"
            + "import org.junit.runners.Suite;\nimport org.junit.runners.Suite.SuiteClasses;\n\n"
            + "+import net.ssehub.kBuildCrawler.git.diff.AllGitDiffTests;\n"
            + "+\n"
            + "+@RunWith(Suite.class)\n"
            + "-@SuiteClasses({ GitUtilsTest.class })\n"
            + "+@SuiteClasses({\n"
            + "+    AllGitDiffTests.class,\n"
            + "+    GitUtilsTest.class })\n"
            + "public class AllGitTests {\n\n}\n"
            + "@@ -12,1 +16,1 @@\n"
            + "+// A Comment\n";
        String[] lines = diffString.split(IOUtils.LINEFEED_REGEX);
        
        FileDiff diff = DiffUtils.parseFileDiff(lines, 0, lines.length);
        Assert.assertNotNull(diff);
        Assert.assertEquals("KbuildCrawler/test/net/ssehub/kBuildCrawler/git/AllGitTests.java", diff.getFile());
        Assert.assertEquals(ChangeType.CHANGED, diff.getFileChange());
        Assert.assertEquals(2, diff.getHunks().size());
        
        InFileDiff hunk = diff.getHunks().get(0);
        Assert.assertEquals(4, hunk.getStartLineBefore());
        Assert.assertEquals(8, hunk.getNumberOfChangesLinesBefore());
        Assert.assertEquals(4, hunk.getStartLineAfter());
        Assert.assertEquals(12, hunk.getNumberOfChangesLinesAfter());
        
        hunk = diff.getHunks().get(1);
        Assert.assertEquals(12, hunk.getStartLineBefore());
        Assert.assertEquals(1, hunk.getNumberOfChangesLinesBefore());
        Assert.assertEquals(16, hunk.getStartLineAfter());
        Assert.assertEquals(1, hunk.getNumberOfChangesLinesAfter());
    }

}
