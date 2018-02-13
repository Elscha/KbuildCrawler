import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import net.ssehub.kBuildCrawler.AllTests;
     * Helper method to read a diff from a text file.
     * @param fileName A text file within the {@link AllTests#TESTDATA} folder containing a diff file.
     * @return The content of the specified file.
     */
    private static String loadDiff(String fileName) {
        File file = new File(AllTests.TESTDATA, fileName);
        String content = null;
        try {
            content = org.apache.commons.io.FileUtils.readFileToString(file, (Charset) null);
            Assert.assertNotNull(content);
        } catch (IOException e) {
            Assert.fail("Could not read file \"" + file.getAbsolutePath() + "\": " + e.getMessage());
        }
        
        return content;
    }
    
    /**
     * Tests {@link DiffUtils#parseDiff(String)} if only one file was added, with one hunk.
     * Tests {@link DiffUtils#parseDiff(String)} if only one file was added, with two hunks.
    
    /**
     * Tests the {@link DiffUtils#parseDiff(String)} method.
     */
    @Test
    public void testParseDiff_MultipleFiles() {
        String diffString = loadDiff("diffMultipleFiles.diff");
        CommitDiff diff = DiffUtils.parseDiff(diffString);
        
        // Whole diff
        Assert.assertNotNull(diff);
        Assert.assertEquals(4, diff.getChangedFiles().size());
        
        // Files
        assertFileDiff(diff.getChangedFiles().get(0),
            "KbuildCrawler/test/net/ssehub/kBuildCrawler/mail/AllMailTests.java", ChangeType.CHANGED, 1);
        assertFileDiff(diff.getChangedFiles().get(1),
            "KbuildCrawler/test/net/ssehub/kBuildCrawler/mail/GZFolderMailSourceTest.java", ChangeType.ADDED, 1);
        assertFileDiff(diff.getChangedFiles().get(2),
            "KbuildCrawler/test/net/ssehub/kBuildCrawler/mail/ZipMailSourceTests.java", ChangeType.CHANGED, 2);
        assertFileDiff(diff.getChangedFiles().get(3),
            "KbuildCrawler/testdata/2016-August-copy.txt.gz", ChangeType.ADDED, 0);
        
    }

    /**
     * Helper method to test a {@link CommitDiff}.
     * @param fDiff The {@link CommitDiff} to test.
     * @param expectedFile The expected relative path of the associated file.
     * @param expectedChange The expected kind of change for the given file.
     * @param expectedHunks The expected number of changed code blocks.
     */
    private void assertFileDiff(FileDiff fDiff, String expectedFile, ChangeType expectedChange, int expectedHunks) {
        Assert.assertNotNull(fDiff);
        Assert.assertEquals(expectedFile, fDiff.getFile());
        Assert.assertEquals(expectedChange, fDiff.getFileChange());
        Assert.assertEquals(expectedHunks, fDiff.getHunks().size());
    }