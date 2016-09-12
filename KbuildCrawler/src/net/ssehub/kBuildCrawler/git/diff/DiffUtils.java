package net.ssehub.kBuildCrawler.git.diff;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kBuildCrawler.io.IOUtils;

/**
 * Utility methods to parse a String and split it into {@link CommitDiff} data objects.
 * @author El-Sharkawy
 *
 */
public class DiffUtils {
    private static final int PATH_OFFSET = 2;
    private static final String NO_FILE = "/dev/null";
    private static final String FILE_DIFF_START = "diff --git ";

    /**
     * Avoid instantiation.
     */
    private DiffUtils() {}
    
    /**
     * Parses a complete diff log for multiple files into a {@link CommitDiff} data object.
     * @param diff The output of a <tt>diff &lt;commit&gt; &lt;commit&gt;</tt> command
     * @return The parsed diff.
     */
    public static CommitDiff parseDiff(String diff) {
        String lines[] = diff.split(IOUtils.LINEFEED_REGEX);
        List<FileDiff> files = new ArrayList<>();
        
        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith(FILE_DIFF_START)) {
                endIndex = i - 1;
                if (endIndex > startIndex) {
                    FileDiff fileDiff = parseFileDiff(lines, startIndex, endIndex);
                    if (null != fileDiff) {
                        files.add(fileDiff);
                    }
                }
                startIndex = i;
            }
        }
        
        // last file
        FileDiff fileDiff = parseFileDiff(lines, endIndex + 1, lines.length - 1);
        if (null != fileDiff) {
            files.add(fileDiff);
        }
        
        return new CommitDiff(files);
    }
    
    /**
     * Extracts changes for a changed file from a diff.
     * @param diffLines All lines from a diff for all files.
     * @param startLine Start line for the current file
     * @param endLine End line for the current file
     * @return A data object for the given file in the diff.
     */
    static FileDiff parseFileDiff(String[] diffLines, int startLine, int endLine) {
        int offset = diffLines[startLine + 2].startsWith("index") == true ? 3 : 2;
        boolean binaryFile = false;
        String fileBefore;
        String fileAfter;
        
        if (diffLines[startLine + offset].startsWith("Binary files ")) {
            binaryFile = true;
            String line = diffLines[startLine + offset];
            fileBefore = line.split(" ")[2];
            fileAfter = line.split(" ")[4];  
        } else {
            fileBefore = diffLines[startLine + offset].split(" ")[1];
            fileAfter = diffLines[startLine + offset + 1].split(" ")[1];   
        }
        
        ChangeType filechange = ChangeType.CHANGED;
        String fileName = null;
        if (NO_FILE.equals(fileBefore) && !NO_FILE.equals(fileAfter)) {
            filechange = ChangeType.ADDED;
            fileName = fileAfter.substring(PATH_OFFSET);
        } else if (!NO_FILE.equals(fileBefore) && NO_FILE.equals(fileAfter)) {
            filechange = ChangeType.DELETED;            
            fileName = fileBefore.substring(PATH_OFFSET);
        } else {
            fileName = fileBefore.substring(PATH_OFFSET);
        }
        
        List<InFileDiff> lineChanges = new ArrayList<>();
        if (!binaryFile) {
            int startIndex = startLine + offset + 2;
            int endIndex = 0;
            for (int i = startLine + offset + 2; i < endLine; i++) {
                String line = diffLines[i];
                if (line.startsWith("@@ -")) {
                    endIndex = i - 1;
                    if (endIndex > startIndex) {
                        InFileDiff hunk = parseInffileDiff(diffLines, startIndex, endIndex);
                        if (null != hunk) {
                            lineChanges.add(hunk);
                        }
                    }
                    startIndex = i;
                }
            }
            // last hunk
            InFileDiff hunk = parseInffileDiff(diffLines, endIndex + 1, endLine);
            if (null != hunk) {
                lineChanges.add(hunk);
            }
        }
        return new FileDiff(fileName, filechange, lineChanges);
    }
    
    /**
     * Extracts one hunk from a File diff
     * @param lines All lines from a diff.
     * @param startLine Starting point for the current hunk
     * @param endLine End line for the current hunk.
     * @return A hunk or <tt>null</tt> in case of any errors.
     */
    static InFileDiff parseInffileDiff(String[] lines, int startLine, int endLine) {
        InFileDiff hunk = null;

        String hunkHeader = lines[startLine];
        String[] headerElements = hunkHeader.split(" ");
        String before = headerElements[1];
        String after = headerElements[2];
        int[] beforeInNumbers = extractLinesFromHunkHeader(before);
        int[] afterInNumbers = extractLinesFromHunkHeader(after);
        
        if (null != beforeInNumbers && null != afterInNumbers) {
            hunk = new InFileDiff(beforeInNumbers[0], beforeInNumbers[1], afterInNumbers[0], afterInNumbers[1]);
        }
        
        return hunk;
    }
    
    /**
     * Extracts the line numbers from a hunk header.
     * @param lineSection A String in form of <tt>@@ from-file-range to-file-range @@ [header]</tt>
     * @return A two tuple in form of (start line, n lines) or <tt>null</tt> in case of errors.
     * @see <a href="http://stackoverflow.com/a/2530012">
     * http://stackoverflow.com/questions/2529441/how-to-read-the-output-from-git-diff/2530012#2530012</a>
     */
    private static int[] extractLinesFromHunkHeader(String lineSection) {
        int[] lines = null;
        if (null != lineSection) {
            // Remove - and +
            lineSection = lineSection.substring(1);
            int pos = lineSection.indexOf(",");
            if (-1 != pos) {
                lines = new int[2];
                lines[0] = Integer.valueOf(lineSection.substring(0, pos));
                lines[1] = Integer.valueOf(lineSection.substring(pos + 1));
            }
        }
        
        return lines;
    }
}
