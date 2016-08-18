package net.ssehub.kBuildCrawler.git.diff;

import java.util.Collections;
import java.util.List;

public class CommitDiff {
    private List<FileDiff> changedFiles;
    
    CommitDiff(List<FileDiff> changedFiles) {
        this.changedFiles = Collections.unmodifiableList(changedFiles);
    }

    public List<FileDiff> getChangedFiles() {
        return changedFiles;
    }
}
