package net.ssehub.kBuildCrawler.git;

import java.io.File;

import net.ssehub.kBuildCrawler.git.mail_parsing.GitData;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Interface for KbuildCrawler for interacting with git.
 *  
 * @author Adam
 */
public class GitInterface {

    private GitRepository repo;
    
    /**
     * Creates a {@link GitInterface} for the given repository location.
     * 
     * @param repoLocation The location for the git repository.
     * 
     * @throws GitException If creating the git repository fails.
     */
    public GitInterface(File repoLocation) throws GitException {
        this.repo = new GitRepository(repoLocation);
    }
    
    /**
     * Returns the path to the working directory of the git repository. This will be where everything is checked out.
     * 
     * @return The source tree path. An existing directory.
     */
    public File getSourceTree() {
        return repo.getWorkingDirectory();
    }
    
    /**
     * Restores the given commit.
     * 
     * @param commitInfo The data about the commit to restore.
     * @param date The date to use when checking out branches. This ensures that branches are not checked out as the
     *          latest version, but the correct version of the failure report is used. Format: "2017-02-12 12:45:34"
     * 
     * @throws GitException If restoring the commit fails.
     */
    public void restoreCommit(GitData commitInfo, String date) throws GitException {
        String remoteUrl = commitInfo.is0DayCommit() ? GitData.ZERO_DAY_GIT_URL : commitInfo.getBase();
        if (remoteUrl == null) {
            throw new GitException("Remote URL is null");
        }
        
        String remoteName = GitRepository.createRemoteName(remoteUrl);
        boolean fetched = false; // we only fetch on-demand; this stores if we already fetched
        
        // add and fetch remote
        if (!repo.getRemotes().contains(remoteName)) {
            repo.addRemote(remoteName, remoteUrl);
            // do not fetch yet; fetches are done with proper commit or branch target later on
        }
        
        String commit = commitInfo.getCommit(); // TODO: use getCommit() or getHead() here?
        if (commitInfo.is0DayCommit()) {
            // only fetch if we don't yet have the branch
            if (!fetched && !repo.containsRemoteBranch(remoteName, commitInfo.get0DayBranch())) {
                repo.fetch(remoteName, commitInfo.get0DayBranch());
                fetched = true;
            }
            commit = repo.getLastCommitOfBranch(remoteName, commitInfo.get0DayBranch());
        }
        
        if (commit == null && commitInfo.getBranch() != null) {
            // if getCommit() is null, then use getBranch() and the date to get the URL
            
            // only fetch if we don't yet have the branch
            if (!fetched && !repo.containsRemoteBranch(remoteName, commitInfo.getBranch())) {
                repo.fetch(remoteName, commitInfo.getBranch());
                fetched = true;
            }
            
            commit = repo.getCommitBefore(remoteName, commitInfo.getBranch(), date);
        }
        if (commit == null) {
            throw new GitException("Both commit and branch are null");
        }
        
        // if we haven't fetched yet, check if we need to fetch in order to get the required commit
        if (!fetched && !repo.containsCommit(commit)) {
            repo.fetch(remoteName, commit);
            fetched = true;
        }
        
        if (!fetched) {
            Logger.get().logDebug("Didn't need to fetch because commit " + commit + " already existed");
        }
        
        repo.checkout(commit); 
    }
    
}
