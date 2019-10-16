package net.ssehub.kBuildCrawler.git.mail_parsing;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import net.ssehub.kBuildCrawler.mail.Mail;

/**
 * Contains all necessary information in a parsed structure to make a compilation problem/error reported by
 * the Kbuild test robot reproduceable.
 * @author El-Sharkawy
 *
 */
public class FailureTrace {

    private Mail mail;
    private GitData gitInfo;
    
    /**
     * Optional: Location of <tt>.config</tt> file for KConfig.
     */
    private ConfigProvider config;
    private List<FileDefect> defects;
    
    /**
     * Constructor for parsed mails of the mailing list.
     * @param mail The original mail of the Kbuild test robot, which does contain relevant GIT information
     *     only in an unparsed structure.
     * @param gitInfo The relevant information of {@link Mail#getContent()} in a parsed structure to reproduce
     *     the reported error.
     * @param config Optional: The used <tt>.config</tt> file of Kconfig, which was used for the report.
     * @param defects Optional: A list of found defects.
     */
    FailureTrace(Mail mail, GitData gitInfo, ConfigProvider config, List<FileDefect> defects) {
        this.mail = mail;
        this.gitInfo = gitInfo;
        this.config = config;
        this.defects = defects;
    }
    
    /**
     * Constructor to use when working without a mailing archive, e.g., with a CSV file.
     * @param gitInfo The relevant information of {@link Mail#getContent()} in a parsed structure to reproduce
     *     the reported error.
     * @param defects Optional: A list of found defects.
     */
    public FailureTrace(GitData gitInfo, List<FileDefect> defects) {
        this.gitInfo = gitInfo;
        this.defects = defects;
    }
    
    /**
     * Returns the original mail, which contains all information of this trace in an unstructured way.
     * @return The original mail.
     */
    public Mail getMail() {
        return mail;
    }

    /**
     * Returns parsed information of how to reproduce an error reported by the Kbuild test robot.
     * @return The parsed git information, like clone url, branch and so on.
     */
    public GitData getGitInfo() {
        return gitInfo;
    }

    /**
     * Returns the used <tt>.config</tt> file of Kconfig, which was used for the report.
     * @return The used <tt>.config</tt> file of Kconfig, maybe <tt>null</tt>.
     */
    public ConfigProvider getConfig() {
        return config;
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
    public String getFormattedDate(boolean useColons) throws DateTimeParseException {
        ZonedDateTime zdt = ZonedDateTime.parse(mail.getDate(), DateTimeFormatter.RFC_1123_DATE_TIME);
        
        String pattern = useColons ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HHmm.ss";
        
        return zdt.format(DateTimeFormatter.ofPattern(pattern));
    }

    @Override
    public String toString() {
        String result =  "From: " + mail.getFrom() + " with subject: " + mail.getSubject()
            + " has problem in:\n" + gitInfo;
        if (null != config) {
            result += "\n" + config;
        }
        if (null != defects && !defects.isEmpty()) {
            for (int i = 0, end = defects.size(); i < end; i++) {
                result += "\n - Defect[" + i + "]: " + defects.get(i);
            }
        }
        return result + "\n";
    }
}
