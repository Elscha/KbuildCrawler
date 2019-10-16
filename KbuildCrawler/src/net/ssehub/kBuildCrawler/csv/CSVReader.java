package net.ssehub.kBuildCrawler.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ssehub.kBuildCrawler.git.FailureTrace;
import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;
import net.ssehub.kBuildCrawler.git.mail_parsing.GitData;
import net.ssehub.kernel_haven.util.io.ITableReader;
import net.ssehub.kernel_haven.util.io.csv.CsvReader;

public class CSVReader {

    private static final char CSV_SEPARATOR = ',';
    private static final String DEFECT_DESCRIPTION = FileDefect.Type.VULNERABILITY.name();
    private static final String GIT_REPOSITORY = "git://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git";
    private static final String GIT_BRANCH = "master";
    
    private ITableReader reader;
    private int commitColumn;
    private int cveColumn;
    private int fileColumn;
    private int funcColumns;
    
    public CSVReader(String path, int commitColumn, int cveColumn, int fileColumn, int funcColumns)
        throws FileNotFoundException {
        
        this.commitColumn = commitColumn;
        this.cveColumn = cveColumn;
        this.fileColumn = fileColumn;
        this.funcColumns = funcColumns;
        reader = new CsvReader(new BufferedReader(new FileReader(path)), CSV_SEPARATOR);
    }
    
    public List<FailureTrace> readFile() {
        List<FailureTrace> result = new ArrayList<FailureTrace>();
        
        FailureTrace lastTrace = null;
        
        try {
            String[] row;
            while ((row = reader.readNextRow()) != null) {
                String commitHash = row[commitColumn];
                String cve = row[cveColumn];
                String path = row[fileColumn];
                int sep = path.lastIndexOf('/');
                String file = path.substring(sep + 1);
                path = path.substring(0, sep + 1);
                
                List<FileDefect> defects = new ArrayList<>();
                for (int i = funcColumns; i < row.length; i++) {
                    String func = row[i];
                    if (!func.isEmpty()) {
                        FileDefect defect = new FileDefect(path, file, func, cve, DEFECT_DESCRIPTION);
                        defects.add(defect);
                    } else {
                        break;
                    }
                }
                
                if (lastTrace != null && lastTrace.getGitInfo().getCommit().equals(commitHash)) {
                    // Add defects to last trace (same commit, but other files)
                    lastTrace.getDefects().addAll(defects);
                } else {
                    // New defect trace (new commit hash)
                    GitData data = new GitData(null, GIT_REPOSITORY, GIT_BRANCH, null, commitHash);
                    lastTrace = new CVEReport(data, defects, cve);
                    result.add(lastTrace);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Remove empty reports (if report dosn't consider at least one function)
        for (int i = result.size() - 1; i >= 0; i--) {
            if (result.get(i).getDefects().isEmpty()) {
                result.remove(i);
            }
        }
        
        return result;
    }
}
