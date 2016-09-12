package net.ssehub.kBuildCrawler.git.diff;

import java.util.Collections;
import java.util.List;

/**
 * Data object for a parsed diff between two different commits: <tt>diff &lt;commit&gt; &lt;commit&gt;</tt>.
 * @author El-Sharkawy
 *
 */
public class CommitDiff {
    private List<FileDiff> changedFiles;
    
    /**
     * Sole constructor for this class.
     * @param changedFiles The list of changed files.
     */
    CommitDiff(List<FileDiff> changedFiles) {
        this.changedFiles = Collections.unmodifiableList(changedFiles);
    }

    /**
     * The list of changed files.
     * @return The list of changed files.
     */
    public List<FileDiff> getChangedFiles() {
        return changedFiles;
    }
}
