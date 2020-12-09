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

/**
 * Parser to parse CVEs collected by own CVE-Extractor tool.
 * @author El-Sharkawy
 *
 */
public class CSVReader2 {

    private static final char CSV_SEPARATOR = ';';
    private static final String GIT_REPOSITORY = "git://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git";
    private static final String GIT_BRANCH = "master";
    
    private ITableReader reader;
    private int commitColumn;
    private int cveColumn;
    private int fileColumn;
    private int linesColumn;
    
    public CSVReader2(String path, int commitColumn, int cveColumn, int fileColumn, int linesColumn)
        throws FileNotFoundException {
        
        this.commitColumn = commitColumn;
        this.cveColumn = cveColumn;
        this.fileColumn = fileColumn;
        this.linesColumn = linesColumn;
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
                String[] lines = row[linesColumn].split("-");
                int start = Integer.valueOf(lines[0]);
                int end = Integer.valueOf(lines[1]);
                
                
                List<FileDefect> defects = new ArrayList<>();
                for (int i = start; i <= end; i++) {
                    defects.add(new FileDefect(path, file, i, 0, cve));
                }
                
                if (lastTrace != null && lastTrace.getGitInfo().getCommit().equals(commitHash)) {
                    // Add defects to last trace (same commit, but other file or at least other chunk)
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
