package net.ssehub.kBuildCrawler.git.plugins;

import java.io.File;

import net.ssehub.kBuildCrawler.git.GitData;

/**
 * Interface for a (stateless) git repository including a plugin for handling this repository.
 * @author El-Sharkawy
 *
 */
public interface IGitPlugin {
    
    /**
     * Specifies the folder where to operate on.
     * @param basePath The parent folder where to download a repository or the folder of the repository itself
     *     if changes shall be done there.
     * @return The result of the change operation, should usually be <tt>basePath</tt>, or the previous
     *     specified folder if the parameter did not point to a valid folder or the folder could not be created.
     */
    public File setBasePath(File basePath);
    
    /**
     * Clones a repository.
     * @param url The url of the repository.
     * @param branch Optional: the branch to be checked out, or <tt>null</tt>.
     * @return The folder of the checked out repository.
     */
    public File clone(String url, String branch);
    
    /**
     * Calculates a diff between the two commits.
     * @param headSHA1 The parent/previous commit.
     * @param commitSHA1 The succeeding commit to compare.
     * @return A textual, not parsed diff.
     * @see <a href="http://stackoverflow.com/a/2530012">
     * http://stackoverflow.com/a/2530012</a>
     */
    public String diff(String headSHA1, String commitSHA1);
    
    /**
     * Checks out the specified commit.
     * @param hash The SHA1 hash of the specified commit.
     * @return <tt>true</tt> if checkout was successful, <tt>false</tt> if not.
     */
    public boolean checkout(String hash);
    
    /**
     * Synchronizes the local repository with its remote repository.
     * @return <tt>true</tt> if update (fetch) was successful, <tt>false</tt> if not.
     */
    public boolean fetch();
    
    /**
     * Switches to a specified branch.
     * @param branch the name of the branch (<tt>master</tt> is the default branch).
     * @return <tt>true</tt> if update (fetch) was successful, <tt>false</tt> if not.
     */
    public boolean swithToBranch(String branch);
    
    /**
     * Highlevel operation: Uses a parsed {@link GitData} object to restore a repository, branch and commit in one step.
     * @param commitInfo A parsed commit info to restore a specific commit as it was part of a report.
     * @return <tt>true</tt> if this operation was successful, <tt>false</tt> if not.
     */
    public boolean restoreCommit(GitData commitInfo);
}
