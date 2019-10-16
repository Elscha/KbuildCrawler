package net.ssehub.kBuildCrawler.csv;

import java.time.format.DateTimeParseException;
import java.util.List;

import net.ssehub.kBuildCrawler.git.FailureTrace;
import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;
import net.ssehub.kBuildCrawler.git.mail_parsing.GitData;

/**
 * Contains reported CVE errors, imported by the {@link CSVReader}.
 * @author El-Sharkawy
 *
 */
public class CVEReport extends FailureTrace {

    private String cve;
    
    protected CVEReport(GitData gitInfo, List<FileDefect> defects, String cve) {
        super(gitInfo, defects);
        this.cve = cve;
    }

    @Override
    public String getFormattedDate(boolean useColons) throws DateTimeParseException {
        // TODO SE: Breaks the interfaces, but this becomes only critical if a branch but no commit has is given
        return cve;
    }
    
    @Override
    public String getDate() {
        return cve;
    }

    @Override
    public String toString() {
        String result = cve;
        
        if (null != getDefects() && !getDefects().isEmpty()) {
            for (int i = 0, end = getDefects().size(); i < end; i++) {
                result += "\n - Defect[" + i + "]: " + getDefects().get(i);
            }
        }
        return result + "\n";
    }
}
