package net.ssehub.kBuildCrawler.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kBuildCrawler.AllTests;
import net.ssehub.kBuildCrawler.git.mail_parsing.FailureTrace;
import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;

/**
 * Tests the {@link CSVReader}.
 * @author El-Sharkawy
 *
 */
public class CSVReaderTest {

    /**
     * Tests the extraction of CVE reports from a CSV file.
     * @throws FileNotFoundException Must not occur, otherwise test file is missing.
     */
    @Test
    public void testReadFile() throws FileNotFoundException {
        File csvFile = new File(AllTests.TESTDATA, "linux-cve-files-functions-git-history.csv");
        CSVReader reader = new CSVReader(csvFile.getAbsolutePath(), 0, 1, 2, 3);
        List<FailureTrace> traces = reader.readFile();
        
        Assert.assertNotNull(traces);
        /*
         * Test file contains
         * - 440 entries
         * - 203 without duplicates (which are merged to one trace)
         * - 145 results without duplicates and at least one reported function
         */
        Assert.assertEquals(146, traces.size());
        for (int i = 0; i < traces.size(); i++) {
            FailureTrace trace = traces.get(i);
            Assert.assertFalse(trace.getDefects().isEmpty());
            for (int j = 0; j < trace.getDefects().size(); j++) {
                FileDefect defect = trace.getDefects().get(j);
                Assert.assertEquals(FileDefect.Type.VULNERABILITY, defect.getType());
                Assert.assertNotNull(defect.getFunction());
            }
        }
    }

}
