package net.ssehub.kBuildCrawler.git;

import java.time.format.DateTimeParseException;
import java.util.List;

import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;
import net.ssehub.kBuildCrawler.git.mail_parsing.GitData;

/**
 * Common interface for reported failures.
 * @author El-Sharkawy
 *
 */
public abstract class FailureTrace {
    private GitData gitInfo;
    
    private List<FileDefect> defects;
    
    protected FailureTrace(GitData gitInfo, List<FileDefect> defects) {
        this.gitInfo = gitInfo;
        this.defects = defects;
    }
    
    /**
     * Returns parsed information of how to reproduce an error reported by the Kbuild test robot.
     * @return The parsed git information, like clone url, branch and so on.
     */
    public GitData getGitInfo() {
        return gitInfo;
    }
    
    /**
     * Returns a list of found defects.
     * @return A list of found defects, should not be <tt>null</tt>.
     */
    public List<FileDefect> getDefects() {
        return defects;
    }
    
    /**
     * Returns the date of this mail as a string with the following format: "2017-02-13 14:34:21".
     * When <code>useColons</code> is <code>false</code>, then the format is "2017-02-13 1434.21" (excel sheet name
     * compatible).
     * 
     * @param useColons Whether colons are allowed in the string or not. 
     * @return A string containing the date of this mail.
     * 
     * @throws DateTimeParseException If the date of the mail could not be parsed.
     */
    public abstract String getFormattedDate(boolean useColons) throws DateTimeParseException;
    
    /**
     * Returns the date or another identifier when the error occurred.
     * @return A date or a year based CVE number.
     */
    public abstract String getDate();
}
