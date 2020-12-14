package net.ssehub.kBuildCrawler.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ssehub.kBuildCrawler.git.FailureTrace;
import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;

class FailureTraceMerger {
    private Map<String, CVEReport> traces = new HashMap<>();

    void addAll(List<FailureTrace> traces) {
        for (FailureTrace failureTrace : traces) {
            add((CVEReport) failureTrace);
        }
    }
    
    void add(CVEReport trace) {
        String commitID = trace.getGitInfo().getCommit();
        CVEReport previousTrace = traces.get(commitID);
        
        if (null == previousTrace) {
            traces.put(commitID, trace);
        } else {
            // One commit that fixes multiple CVEs
            // Defect list should be complete, but to be sure create union
            List<FileDefect> defects = previousTrace.getDefects();
            for (FileDefect newDefect : trace.getDefects()) {
                if (!defects.contains(newDefect)) {
                    defects.add(newDefect);
                }
            }
            
            String cves = previousTrace.getDate() + ", " + trace.getDate();
            previousTrace.setName(cves);
        }
    }
    
    int size() {
        return traces.size();
    }
    
    List<FailureTrace> getTraces() {
        return new ArrayList<>(traces.values());
    }
}
