package net.ssehub.kBuildCrawler.mail;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.ssehub.kBuildCrawler.io.IOUtils;

/**
 * Static utility functions for mails.
 * @author El-Sharkawy
 *
 */
public class MailUtils {
    
    public static final String NEW_ERROR_PREFIX = ">> ";
    
    static final String COMPILATION_WARNINGS_REGEX = "^.*\\.c\\:\\p{Digit}+:\\p{Digit}+.*$";
    private static final String KBUILD_TEST_ROBOT_ADDRESS1 = "lkp at intel.com (kbuild test robot)";
    private static final String KBUILD_TEST_ROBOT_ADDRESS2 = "fengguang.wu at intel.com (kbuild test robot)";
    private static final String COMPILATION_ERRORS_CONTENT = "compilation terminated.";

    static boolean isFromKbuildRobot(String from) {
        boolean isFromRobot = from.equals(KBUILD_TEST_ROBOT_ADDRESS1);
        
        if (!isFromRobot) {
            isFromRobot = from.equals(KBUILD_TEST_ROBOT_ADDRESS2);
        }
        
        return isFromRobot;
    }
    
    /**
     * Splits a list of parsed mails to mails from the KBuild test robot and other mails.
     * @param allMails The mails to split.
     * @param acceptRobotMails <tt>true</tt> returns only mails from the Kbuild test robot, <tt>false</tt> all other
     *     mails not from the Kbuild test robot.
     * @return a subset of the mails passed to this function.
     */
    public static List<Mail> filterForKbuildTestRobot(List<Mail> allMails, boolean acceptRobotMails) {
        Stream<Mail> mailStream = allMails.stream();
        mailStream = acceptRobotMails ?
            mailStream.filter(mail -> isFromKbuildRobot(mail.getFrom())) :
            mailStream.filter(mail -> !isFromKbuildRobot(mail.getFrom()));
        return mailStream.collect(Collectors.toList());
    }
    
    static boolean hasCompilerProblem(Mail mail) {
        boolean hasProblem = false;
        
        String[] lines = mail.getContent().split(IOUtils.LINEFEED_REGEX);
        for (int i = 0; i < lines.length && !hasProblem; i++) {
            if (lines[i].startsWith(">>")) {
                hasProblem = lines[i].matches(COMPILATION_WARNINGS_REGEX);
            }
        }
        
        return hasProblem;
    }
    
    /**
     * Filters a list of parsed mails for mails containing compilation errors/problems
     * @param allMails The mails to split.
     * @param onlyErrors <tt>true</tt> returns only mails containing compilation errors, <tt>false</tt> mails may also
     *   contain warnings.
     * @return a subset of the mails passed to this function.
     */
    public static List<Mail> filterForCompilationProblems(List<Mail> allMails, boolean onlyErrors) {
        Stream<Mail> mailStream = allMails.stream();
        mailStream = onlyErrors ?
            mailStream.filter(mail -> mail.getContent().contains(COMPILATION_ERRORS_CONTENT)) :
            mailStream.filter(mail -> hasCompilerProblem(mail));
        return mailStream.collect(Collectors.toList());
    }
}
