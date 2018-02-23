package net.ssehub.kBuildCrawler.git.mail_parsing;

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
     * Solve constructor for this class.
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
