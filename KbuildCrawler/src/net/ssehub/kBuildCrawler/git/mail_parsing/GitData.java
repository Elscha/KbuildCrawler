package net.ssehub.kBuildCrawler.git.mail_parsing;

import java.util.Objects;

/**
 * Parsed information of how to reproduce an error reported by the Kbuild test robot.
 * There exist two ways to reproduce the data (depends on the information available):
 * <ul>
 *   <li>Cloning the repository via {@link #getBase()} and {@link #getCommit()}</li>
 *   <li>Download from {@link #getUrl()}</li>
 * </ul>
 * @author El-Sharkawy
 *
 */
public class GitData {
    public static final String ZERO_DAY_GIT_URL = "https://github.com/0day-ci/linux.git";
    private static final String ZERO_DAY_URL = "https://github.com/0day-ci/linux";
    
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
    
    /**
     * Returns the url from where to download the project. 
     * @return The url from where to download the project, maybe <tt>null</tt>.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the url of the whole git repository.
     * @return the url of the whole git repository, maybe <tt>null</tt>.
     */
    public String getBase() {
        return base;
    }

    /**
     * Returns the specified branch.
     * @return The specified branch or <tt>null</tt>. If not <tt>null</tt>, than also {@link #getBase()} must not
     *     be <tt>null</tt>.
     */
    public String getBranch() {
        return branch;
    }

    /**
     * Optional head commit for a problematic commit. Maybe used for creating a diff.
     * @return Head commit for a problematic commit, maybe <tt>null</tt>.
     */
    public String getHead() {
        return head;
    }

    /**
     * Problematic commit.
     * @return Problematic commit, maybe <tt>null</tt>.
     */
    public String getCommit() {
        return commit;
    }
    
    /**
     * Returns whether the data can be used for cloning (preferred way) or downloading a repository.
     * @return <tt>true</tt>Repository can be cloned via {@link #getBase()} and {@link #getCommit()},
     * <tt>false</tt> repository must be downloaded from {@link #getUrl()}.
     */
    public boolean cloningPossible() {
        return getBase() != null && getCommit() != null;
    }
    
    /**
     * Returns whether the GitData points to a (set of) patches in the 0Day commit repository. If <tt>true</tt>
     * {@link #ZERO_DAY_GIT_URL} should be used instead of {@link #getBase()} or {@link #getUrl()}.
     * @return <tt>true</tt> The data represents a (set of) patches in the 0Day commit repository.
     */
    public boolean is0DayCommit() {
        return url != null && url.startsWith(ZERO_DAY_URL);
    }
    
    public String get0DayBranch() {
        String branch = null;
        
        if (is0DayCommit()) {
            // Remove https://github.com/0day-ci/linux/commits/ -> first 41 chars
            branch = url.substring(41);
        }
        
        return branch;
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

    @Override
    public int hashCode() {
        return Objects.hash(base, branch, commit, head, url);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GitData)) {
            return false;
        }
        GitData other = (GitData) obj;
        return Objects.equals(base, other.base) && Objects.equals(branch, other.branch)
                && Objects.equals(commit, other.commit) && Objects.equals(head, other.head)
                && Objects.equals(url, other.url);
    }
    
    
}
