package net.ssehub.kBuildCrawler.git.diff;

/**
 * Utility methods to parse a String and split it into {@link CommitDiff} data objects.
 * @author El-Sharkawy
 *
 */
public class DiffUtils {
    private static final int PATH_OFFSET = 2;

    /**
     * Avoid instantiation.
     */
    private DiffUtils() {}
    
    public static CommitDiff parseDiff(String diff) {
        return null;
    }
    
    public static FileDiff parseFileDiff(String[] diff, int startLine, int endLine) {
        String fileBefore = diff[startLine + 3].split(" ")[1];
        String fileAfter = diff[startLine + 4].split(" ")[1];
        ChangeType filechange = ChangeType.CHANGED;
        String fileName = null;
        if ("/dev/null".equals(fileBefore) && !"/dev/null".equals(fileAfter)) {
            filechange = ChangeType.ADDED;
            fileName = fileAfter.substring(PATH_OFFSET);
        } else if (!"/dev/null".equals(fileBefore) && "/dev/null".equals(fileAfter)) {
            filechange = ChangeType.DELETED;            
            fileName = fileBefore.substring(PATH_OFFSET);
        } else {
            fileName = fileBefore.substring(PATH_OFFSET);
        }
        
        return new FileDiff(fileName, filechange);
    }
}
