package net.ssehub.kBuildCrawler.metrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.config.DefaultSettings;

public class KernelHavenProcessRunnerTest {
    
    @Test
    public void testPrepareConfiguration() throws FileNotFoundException, IOException {
        KernelHavenProcessRunner runner = new KernelHavenProcessRunner();
        Properties baseProperties = new Properties();
        baseProperties.put("regEx", ".*\\.(c|h)");
        File path = new File("A/path");
        Properties preparedProperties = runner.prepareConfiguration(path, null, baseProperties);
        
        // Test path
        Assert.assertNotNull(preparedProperties.get(DefaultSettings.SOURCE_TREE.getKey()));
        Assert.assertEquals(path.getAbsolutePath(), preparedProperties.get(DefaultSettings.SOURCE_TREE.getKey()));
        
        // Test regex
        Assert.assertNotNull(preparedProperties.get("regEx"));
        Assert.assertEquals(".*\\.(c|h)", preparedProperties.get("regEx"));
        
        // Test regEx of default config
        Assert.assertNotNull(preparedProperties.get(DefaultSettings.CODE_EXTRACTOR_FILE_REGEX.getKey()));
        Assert.assertEquals(".*\\.(c|h)", preparedProperties.get(DefaultSettings.CODE_EXTRACTOR_FILE_REGEX.getKey()));
        
        // Test writing and reading of prepared properties
        Writer sWriter = new StringWriter();
        preparedProperties.store(sWriter, null);
        Properties readProperties = new Properties();
        Reader sReader = new StringReader(sWriter.toString());
        readProperties.load(sReader);
        
        // Test again
        Assert.assertNotNull(readProperties.get(DefaultSettings.SOURCE_TREE.getKey()));
        Assert.assertEquals(path.getAbsolutePath(), readProperties.get(DefaultSettings.SOURCE_TREE.getKey()));
        
        // Test regex
        Assert.assertNotNull(readProperties.get("regEx"));
        Assert.assertEquals(".*\\.(c|h)", readProperties.get("regEx"));
        
        // Test regEx of default config
        Assert.assertNotNull(readProperties.get(DefaultSettings.CODE_EXTRACTOR_FILE_REGEX.getKey()));
        Assert.assertEquals(".*\\.(c|h)", readProperties.get(DefaultSettings.CODE_EXTRACTOR_FILE_REGEX.getKey()));
    }

}
