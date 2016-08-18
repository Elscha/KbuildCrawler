package net.ssehub.kBuildCrawler.git;

import java.util.List;

import net.ssehub.kBuildCrawler.mail.Mail;

public class FailureTrace {

    private Mail mail;
    private GitData gitInfo;
    
    /**
     * Optional: Location of .config file for KConfig.
     */
    private ConfigProvider config;
    private List<FileDefect> defects;
    
    public FailureTrace(Mail mail, GitData gitInfo, ConfigProvider config, List<FileDefect> defects) {
        this.mail = mail;
        this.gitInfo = gitInfo;
        this.config = config;
        this.defects = defects;
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
