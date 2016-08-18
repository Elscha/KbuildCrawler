package net.ssehub.kBuildCrawler.git.diff;

import java.util.Collections;
import java.util.List;

public class FileDiff {
    
    private ChangeType fileChange;
    private String file;
    private List<InFileDiff> hunks;

    FileDiff(String file, ChangeType fileChange, List<InFileDiff> hunks) {
        this.file = file;
        this.fileChange = fileChange;
        this.hunks = Collections.unmodifiableList(hunks);
    }

    public String getFile() {
        return file;
    }

    public ChangeType getFileChange() {
        return fileChange;
    }

    public List<InFileDiff> getHunks() {
        return hunks;
    }
}
