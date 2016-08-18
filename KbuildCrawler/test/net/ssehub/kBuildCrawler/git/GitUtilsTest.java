package net.ssehub.kBuildCrawler.git;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kBuildCrawler.git.GitUtils;


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
        Assert.assertTrue(defect.matches(GitUtils.DEFECT_REGEX));
    }

}
