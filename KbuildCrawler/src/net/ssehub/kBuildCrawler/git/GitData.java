package net.ssehub.kBuildCrawler.git;

public class GitData {
    private String url;
    private String base;
    private String branch;
    private String head;
    private String commit;
    
    /**
     * Single constructor of this class, all parameters are optional and, thus, may be <tt>null</tt>.
     * @param url An url from where to download the project.
     * @param base A git repository, if specified also <tt>branch</tt> must be specified.
     * @param branch The branch within the <tt>base</tt> git repository.
     * @param head The base commit (sha-1 tag).
     * @param commit The commit (sha-1 tag).
     */
    public GitData(String url, String base, String branch, String head, String commit) {
        this.url = url;
        this.base = base;
        this.branch = branch;
        this.head = head;
        this.commit = commit;
    }
    
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        if (null != url) {
            result.append("URL: ");
            result.append(url);
        }
        if (null != base) {
            if (result.length() > 0) {
                result.append("\n");                
            }
            result.append("BASE: ");
            result.append(base);
        }
        if (null != branch) {
            if (result.length() > 0) {
                result.append("\n");                
            }
            result.append("BRANCH: ");
            result.append(branch);
        }
        if (null != head) {
            if (result.length() > 0) {
                result.append("\n");                
            }
            result.append("HEAD: ");
            result.append(head);
        }
        if (null != commit) {
            if (result.length() > 0) {
                result.append("\n");                
            }
            result.append("Commit: ");
            result.append(commit);
        }
        
        return result.toString();
    }

}
