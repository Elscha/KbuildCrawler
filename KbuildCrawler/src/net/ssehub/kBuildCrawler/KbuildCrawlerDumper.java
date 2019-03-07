package net.ssehub.kBuildCrawler;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import net.ssehub.kBuildCrawler.git.GitException;
import net.ssehub.kBuildCrawler.git.GitInterface;
import net.ssehub.kBuildCrawler.git.mail_parsing.FailureTrace;
import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;
import net.ssehub.kBuildCrawler.git.mail_parsing.GitData;
import net.ssehub.kBuildCrawler.metrics.IsFunctionChecker;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Logger.Level;
import net.ssehub.kernel_haven.util.Timestamp;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.io.ITableWriter;
import net.ssehub.kernel_haven.util.io.TableElement;
import net.ssehub.kernel_haven.util.io.TableRow;
import net.ssehub.kernel_haven.util.io.csv.CsvWriter;

public class KbuildCrawlerDumper {
    
    public final static File TESTDATA = new File("testdata");
    private static final File TEST_ARCHIVE = new File(TESTDATA, "2016-August.txt.gz");

    public static void main(String[] args) throws Exception {
        File destFile = new File(Timestamp.INSTANCE.getFilename("MailCrawlerDump", "csv"));
        FileOutputStream out = new FileOutputStream(Timestamp.INSTANCE.getFilename("MailCrawler", "log"));
        Logger.get().clearAllTargets();
        Logger.get().addTarget(out);
        Logger.get().setLevel(Level.DEBUG);
        
        File gitFolder = new File("gitTest");
        
        File[] archives = null;
        if (args.length >= 1) {
            System.err.println("Mailinglist items: " + args[0]);
            String[] fileArguments = args[0].split(":");
            
            if (fileArguments.length == 1) {
                int start = fileArguments[0].lastIndexOf('/')  + 1;
                int end = fileArguments[0].indexOf('.', start);
                String name = fileArguments[0].substring(start, end);
                destFile = new File("MailCrawlerDump-" + name + ".csv");
            }
            
            archives = new File[fileArguments.length];
            for (int i = 0; i < fileArguments.length; i++) {
                archives[i] = new File(fileArguments[i]);
                
                if (!archives[i].isFile()) {
                    System.err.println(archives[i] + " is not a valid file");
                    Logger.get().logError(archives[i] + " is not a valid file");
                }
            }
        }
        
        if (archives == null || archives.length == 0) {
            archives = new File[] {TEST_ARCHIVE};
            if (!TEST_ARCHIVE.isFile()) {
                Logger.get().logError(TEST_ARCHIVE + " is not a valid file");
                System.exit(0);
            }
        }
        
        if (args.length >= 2) {
            gitFolder = new File(args[1]);
        }
        
        List<FailureTrace> failures = new LinkedList<FailureTrace>();
        for (int i = 0; i < archives.length; i++) {
            failures.addAll(KbuildCrawler.readMails(archives[i]));
        }
        
        try (ITableWriter tableOut = new CsvWriter(new FileOutputStream(destFile))) {
            
            GitInterface git = new GitInterface(gitFolder);
            IsFunctionChecker checker = new IsFunctionChecker(git.getSourceTree());
            
            for (FailureTrace fail : failures) {
                String date = fail.getMail().getDate();
                GitData gitInfo = fail.getGitInfo();
                String commit = gitInfo.getCommit();
                if (commit == null) {
                    commit = gitInfo.getBranch();
                }
                
                for (FileDefect defect : fail.getDefects()) {
                    File file = new File(defect.getPath() + defect.getFile());
                    
                    // try to find the function name
                    String functionName = null;
                    try {
                        Logger.get().logInfo("Restoring commit...");
                        long t0 = System.currentTimeMillis();
                        git.restoreCommit(gitInfo, fail.getFormattedDate(true));
                        long duration = System.currentTimeMillis() - t0;
                        Logger.get().logInfo("Restoring commit took " + Util.formatDurationMs(duration));
                        
                        functionName = checker.getFunctionName(file, defect.getLine());
                        
                        Logger.get().logInfo("Found function name " + functionName);
                        
                    } catch (GitException e) {
                        Logger.get().logException("Could not restore commit", e);
                    }
                    
                    Dump d = new Dump(date, gitInfo.getBase(), commit, defect.getType().name(),
                            defect.getPath() + defect.getFile(), defect.getLine(), functionName,
                            defect.getDescription());
                    tableOut.writeObject(d);
                }
            }
            
        }
        
        out.close();
        
        File here = new File(".");
        System.err.print(here.getAbsolutePath() + " finished.");
    }
    
    @TableRow
    public static class Dump {
        
        private String date;
        
        private String repo;
        
        private String commit;
        
        private String type;
        
        private String file;
        
        private int line;
        
        private String functionName;
        
        private String description;

        public Dump(String date, String repo, String commit, String type, String file, int line, String functionName,
                String description) {
            this.date = date;
            this.repo = repo;
            this.commit = commit;
            this.type = type;
            this.file = file;
            this.line = line;
            this.functionName = functionName;
            this.description = description;
        }

        @TableElement(index = 0, name = "Date")
        public String getDate() {
            return date;
        }

        @TableElement(index = 1, name = "Repository")
        public String getRepo() {
            return repo;
        }

        @TableElement(index = 2, name = "Commit")
        public String getCommit() {
            return commit;
        }

        @TableElement(index = 3, name = "Type")
        public String getType() {
            return type;
        }

        @TableElement(index = 4, name = "File")
        public String getFile() {
            return file;
        }

        @TableElement(index = 5, name = "Line")
        public int getLine() {
            return line;
        }
        
        @TableElement(index = 6, name = "Function")
        public String getFunctionName() {
            return functionName;
        }

        @TableElement(index = 7, name = "Description")
        public String getDescription() {
            return description;
        }
        
    }
    
}

