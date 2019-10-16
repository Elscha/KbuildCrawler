package net.ssehub.kBuildCrawler.git.mail_parsing;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import net.ssehub.kBuildCrawler.git.FailureTrace;
import net.ssehub.kBuildCrawler.mail.Mail;

/**
 * Contains all necessary information in a parsed structure to make a compilation problem/error reported by
 * the Kbuild test robot reproduceable.
 * @author El-Sharkawy
 *
 */
public class MailReport extends FailureTrace {

    private Mail mail;
    
    /**
     * Optional: Location of <tt>.config</tt> file for KConfig.
     */
    private ConfigProvider config;
    
    /**
     * Constructor for parsed mails of the mailing list.
     * @param mail The original mail of the Kbuild test robot, which does contain relevant GIT information
     *     only in an unparsed structure.
     * @param gitInfo The relevant information of {@link Mail#getContent()} in a parsed structure to reproduce
     *     the reported error.
     * @param config Optional: The used <tt>.config</tt> file of Kconfig, which was used for the report.
     * @param defects Optional: A list of found defects.
     */
    MailReport(Mail mail, GitData gitInfo, ConfigProvider config, List<FileDefect> defects) {
        super(gitInfo, defects);
        this.mail = mail;
        this.config = config;
    }
    
    /**
     * Returns the original mail, which contains all information of this trace in an unstructured way.
     * @return The original mail.
     */
    public Mail getMail() {
        return mail;
    }

    /**
     * Returns the used <tt>.config</tt> file of Kconfig, which was used for the report.
     * @return The used <tt>.config</tt> file of Kconfig, maybe <tt>null</tt>.
     */
    public ConfigProvider getConfig() {
        return config;
    }
    
    @Override
    public String getDate() {
        return mail.getDate();
    }
    
    @Override
    public String getFormattedDate(boolean useColons) throws DateTimeParseException {
        ZonedDateTime zdt = ZonedDateTime.parse(mail.getDate(), DateTimeFormatter.RFC_1123_DATE_TIME);
        
        String pattern = useColons ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HHmm.ss";
        
        return zdt.format(DateTimeFormatter.ofPattern(pattern));
    }

    @Override
    public String toString() {
        String result =  "From: " + mail.getFrom() + " with subject: " + mail.getSubject()
            + " has problem in:\n" + getGitInfo();
        if (null != config) {
            result += "\n" + config;
        }
        if (null != getDefects() && !getDefects().isEmpty()) {
            for (int i = 0, end = getDefects().size(); i < end; i++) {
                result += "\n - Defect[" + i + "]: " + getDefects().get(i);
            }
        }
        return result + "\n";
    }
}
