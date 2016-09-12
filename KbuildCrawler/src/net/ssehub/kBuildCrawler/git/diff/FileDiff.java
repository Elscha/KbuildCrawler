package net.ssehub.kBuildCrawler.git.diff;

import java.util.Collections;
import java.util.List;

/**
 * data object for a file diff, containing only changes within the same file.
 * @author El-Sharkawy
 *
 */
public class FileDiff {
    
    private ChangeType fileChange;
    private String file;
    private List<InFileDiff> hunks;

    /**
     * Sole constructor.
     * @param file The file (relative path inside the repository) to which this diff belongs to
     * @param fileChange The kind of change.
     * @param hunks A list of changed blocks, maybe empty in case of a binary file.
     */
    FileDiff(String file, ChangeType fileChange, List<InFileDiff> hunks) {
        this.file = file;
        this.fileChange = fileChange;
        this.hunks = Collections.unmodifiableList(hunks);
    }

    /**
     * The file (relative path inside the repository) to which this diff belongs to.
     * @return The file (relative path inside the repository) to which this diff belongs to
     */
    public String getFile() {
        return file;
    }

    /**
     * The kind of change.
     * @return The kind of change.
     */
    public ChangeType getFileChange() {
        return fileChange;
    }

    /**
     *  A list of changed blocks, maybe empty in case of a binary file.
     * @return  A list of changed blocks, maybe empty in case of a binary file.
     */
    public List<InFileDiff> getHunks() {
        return hunks;
    }
}
