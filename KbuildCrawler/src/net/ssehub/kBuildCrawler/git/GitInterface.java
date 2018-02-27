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
     * 
     * @throws GitException If restoring the commit fails.
     */
    public void restoreCommit(GitData commitInfo) throws GitException {
        String remoteUrl = commitInfo.getBase();
        
        if (remoteUrl == null) {
            throw new GitException("Remote URL is null");
        }
        
        String remoteName = GitRepository.createRemoteName(remoteUrl);
        
        String commit = commitInfo.getCommit(); // TODO: use getCommit() or getHead() here?
        // if getCommit() is null, then use getBranch()
        if (commit == null) {
            commit = commitInfo.getBranch();
        }
        if (commit == null) {
            throw new GitException("Both commit and branch are null");
        }
        
        if (!repo.getRemotes().contains(remoteName)) {
            repo.addRemote(remoteName, remoteUrl);
        }
        repo.fetch(remoteName);
        repo.checkout(commit); 
    }
    
}
