package net.ssehub.kBuildCrawler.git.diff;

import net.ssehub.kBuildCrawler.io.IOUtils;

public class FileDiff {
    
    private ChangeType fileChange;
    private String file;

    FileDiff(String diff) {
        String[] lines = diff.split(IOUtils.MAIL_LINEFEED_REGEX);
    }
}
