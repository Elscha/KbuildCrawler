package net.ssehub.kBuildCrawler.git;

import java.io.File;

import net.ssehub.kBuildCrawler.git.mail_parsing.GitData;

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
        String remoteUrl = commitInfo.getBase();
        if (remoteUrl == null) {
            throw new GitException("Remote URL is null");
        }
        
        String remoteName = GitRepository.createRemoteName(remoteUrl);
        
        // add and fetch remote
        if (!repo.getRemotes().contains(remoteName)) {
            repo.addRemote(remoteName, remoteUrl);
        }
        repo.fetch(remoteName);
        
        String commit = commitInfo.getCommit(); // TODO: use getCommit() or getHead() here?
        if (commit == null && commitInfo.getBranch() != null) {
            // if getCommit() is null, then use getBranch() and the date to get the URL
            commit = repo.getCommitBefore(remoteName, commitInfo.getBranch(), date);
        }
        if (commit == null) {
            throw new GitException("Both commit and branch are null");
        }
        
        repo.checkout(commit); 
    }
    
}
