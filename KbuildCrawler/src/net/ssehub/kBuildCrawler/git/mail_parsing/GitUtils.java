package net.ssehub.kBuildCrawler.git.mail_parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.kBuildCrawler.io.IOUtils;
import net.ssehub.kBuildCrawler.mail.Mail;
import net.ssehub.kBuildCrawler.mail.MailUtils;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Utility methods to extract relevant git information from {@link Mail}s.
 * @author El-Sharkawy
 */
public class GitUtils {
    
    static final String CONFIG_REGEX = "^URL: <(.*obj)>$";
    
    private static final String C_FILE_PATH = "(.*/)+([^/]*\\.c)";
    private static final String NUMBER = "(\\p{Digit}+)";
    private static final String RANGE = NUMBER + "(\\-" + NUMBER + ")?";
    static final String DEFECT_REGEX = "^>> " + C_FILE_PATH + ":" + NUMBER + "(:" + RANGE + ")?: (.*)$";
    
    private static final String URL_PREFIX = "url: ";
    private static final String BASE_PREFIX = "base: ";
    private static final String TREE_PREFIX = "tree: ";
    private static final String HEAD_PREFIX = "head: ";
    private static final String COMMIT_PREFIX = "commit: ";
    
    /**
     * Extracts the {@link GitData} (information how to download and restore a a tested Linux version)
     * from one {@link Mail} from Kbuild test robot.
     * @param lines {@link Mail#getContent()} separated into single lines.
     * @return The {@link GitData} (information how to download and restore a a tested Linux version)
     */
    static GitData extractGitSettings(String[] lines) {
        String url = null;
        String base = null;
        String branch = null;
        String head = null;
        String commit = null;
        
        try {
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].startsWith(URL_PREFIX)) {
                    url = lines[i].substring(URL_PREFIX.length()).trim();
                }
                if (lines[i].startsWith(BASE_PREFIX)) {
                    String line = lines[i].substring(BASE_PREFIX.length()).trim();
                    String[] elements = line.split(" ");
                    base = elements[0];
                    branch = elements[1];
                }
                if (lines[i].startsWith(TREE_PREFIX)) {
                    String line = lines[i].substring(TREE_PREFIX.length()).trim();
                    String[] elements = line.split(" ");
                    base = elements[0];
                    branch = elements[1];
                }
                if (lines[i].startsWith(HEAD_PREFIX)) {
                    String line = lines[i].substring(HEAD_PREFIX.length()).trim();
                    String[] elements = line.split(" ");
                    head = elements[0];
                }
                if (lines[i].startsWith(COMMIT_PREFIX)) {
                    String line = lines[i].substring(COMMIT_PREFIX.length()).trim();
                    String[] elements = line.split(" ");
                    commit = elements[0];
                }
            }
        } catch (ArrayIndexOutOfBoundsException exc) {
            Logger.get().logException("Could not parse :" + Arrays.toString(lines), exc);
        }
        
        
        GitData data = new GitData(url, base, branch, head, commit);
        return data;
    }
    
    /**
     * Extracts the {@link ConfigProvider} (location of a <tt>.config</tt> Kconfig file, which was used for a report)
     * from one {@link Mail} from Kbuild test robot.
     * @param lines {@link Mail#getContent()} separated into single lines.
     * @return The {@link ConfigProvider} (location of a <tt>.config</tt> Kconfig file, which was used for a report)
     */
    static ConfigProvider extractConfigURL(String[] lines) {
        ConfigProvider config = null;
        
        // Attachment stays usually at the end -> performance optimization
        Pattern pattern = Pattern.compile(CONFIG_REGEX);
        for (int i = lines.length - 1; i >= 0 && null == config; i--) {
            Matcher matcher = pattern.matcher(lines[i]);
            if (matcher.find()) {
                config = new ConfigProvider(matcher.group(1));
            }
        }
        
        return config;
    }
    
    /**
     * Extracts the reported failures from one {@link Mail} from Kbuild test robot.
     * @param lines lines {@link Mail#getContent()} separated into single lines.
     * @return The reported failures
     */
    static List<FileDefect> extractAffectedFiles(String[] lines) {
        List<FileDefect> defects = new ArrayList<>();
        
        Pattern pattern = Pattern.compile(DEFECT_REGEX);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(MailUtils.NEW_ERROR_PREFIX)) {
                Matcher matcher = pattern.matcher(lines[i]);
                if (matcher.find()) {
                    String path = matcher.group(1);
                    String file = matcher.group(2);
                    String lineNo = matcher.group(3);
                    int line = null != lineNo ? Integer.valueOf(lineNo) : 0;
                    int charPos = 0;
                    if (matcher.groupCount() == 6) {
                        String charNo = matcher.group(5);
                        if (null != charNo) {
                            charPos = Integer.valueOf(charNo);
                        }
                    }
                    String description = matcher.group(matcher.groupCount());
                    defects.add(new FileDefect(path, file, line, charPos, description));
                } else {
                    Logger.get().logError("Could not parse: " + lines[i]);
                }
            }
        }
        
        return defects;
    }
    
    /**
     * Converts a list of mails from the Kbuild test robot to Git {@link FailureTrace}s, which contain all
     * necessary information to reproduce the reported error in a structured way.
     * @param kbuildMails Compilation error/warnings reported by the Kbuild test robot.
     * @return The given information in a parsed and structured way to make the report reproduceable.
     */
    public static List<FailureTrace> convertToTraces(List<Mail> kbuildMails) {
        List<FailureTrace> traces = new ArrayList<>();
        
        for (Mail mail: kbuildMails) {
            String[] lines = mail.getContent().split(IOUtils.LINEFEED_REGEX);
            GitData gitInfo = extractGitSettings(lines);
            ConfigProvider config = extractConfigURL(lines);
            List<FileDefect> defects = extractAffectedFiles(lines);
            traces.add(new FailureTrace(mail, gitInfo, config, defects));
        }
        
        return traces;
    }

}
