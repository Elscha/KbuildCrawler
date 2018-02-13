package net.ssehub.kBuildCrawler.git.plugins;

import java.io.File;

/**
 * Implements a git plugin, which stores the state to avoid multiple checkouts of the same repository.<br/>
 * Can also re-use already cloned repositories (cloned in a previous run or done by an external tool).
 * @author El-Sharkawy
 *
 */
public class StateFullGitPlugin extends AbstractGitPlugin {

    private GitCmdPlugin delegate;
    private File basePath;
    private String remoteURL;
    
    /**
     * Single constructor for this class.
     * @param plugin Another git plugin which is used by this plugin (decorator pattern).
     * @param basePath The base path, where to checkout a newly downloaded repository (parent folder).
     */
    public StateFullGitPlugin(GitCmdPlugin plugin, File basePath) {
        delegate = plugin;
        delegate.setBasePath(basePath);
        this.basePath = basePath;
    }
    
    /**
     * Returns the URL of the repository handled by this plugin.
     * @return The clone url fo the
     */
    public String getRepositoryURL() {
        return remoteURL;
    }

    @Override
    public File setBasePath(File basePath) {
        // Not needed, handled this plugin's state
        return basePath;
    }

    @Override
    public File clone(String url, String branch) {
        if (null == remoteURL) {
            File oldPath = basePath;

            int start = url.lastIndexOf("/");
            int end = url.lastIndexOf(".git");
            String repoName = null;
            if (start > -1 && end > -1) {
                repoName = url.substring(start, end);
            }
            if (null != repoName && (basePath = new File(basePath, repoName)).exists()) {
                delegate.setBasePath(basePath);
                // Maybe multiple repositories with same name exist, verify that the correct repository is reused.
                boolean correctRepository = delegate.getRemoteURL().equals(url);
                if (!correctRepository) {
                    int n = 0;
                    File alternativeFolder = new File(oldPath, repoName + "_" + n);
                    while (!correctRepository && alternativeFolder.exists()) {
                        delegate.setBasePath(alternativeFolder);
                        correctRepository = delegate.getRemoteURL().equals(url);
                        if (!correctRepository) {
                            n++;
                            alternativeFolder = new File(oldPath, repoName + "_" + n);
                        }
                    }
                    
                    if (correctRepository && alternativeFolder.exists()) {
                        reuseClonedRepository(alternativeFolder, branch);
                    } else {
                        // Repository does not exist -> clone it
                        if (alternativeFolder.exists()) {
                            alternativeFolder = new File(oldPath, repoName + "_" + ++n);
                        }
                        basePath = delegate.clone(url, branch, alternativeFolder);  
                    }
                } else {
                    /* Repository already downloaded -> switch to desired branch,
                     * update (fetch) the repository and use it.
                     */
                    reuseClonedRepository(basePath, branch);
                }
            } else {
                // Repository does not exist -> clone it
                basePath = delegate.clone(url, branch);                
            }
            
            if (oldPath != basePath && basePath != null && basePath.exists()) {
                remoteURL = url;
                delegate.setBasePath(basePath);
            }
        }
        
        return basePath;
    }

    /**
     * Part of {@link #clone()} to reuse an already cloned repository. Updates the local data and switches automatically
     * to the specified branch.
     * @param basePath The local path of the cloned repository to use.
     * @param branch Optional: a branch to use.
     */
    private void reuseClonedRepository(File basePath, String branch) {
        delegate.setBasePath(basePath);
        if (null != branch) {
            delegate.swithToBranch(branch);
        } else {
            delegate.swithToBranch(AbstractGitPlugin.DEFAULT_BRANCH);
        }
        delegate.fetch();
    }

    @Override
    public String diff(String headSHA1, String commitSHA1) {
        return delegate.diff(headSHA1, commitSHA1);
    }

    @Override
    public boolean checkout(String hash) {
        return delegate.checkout(hash);
    }

    @Override
    public boolean fetch() {
        return delegate.fetch();
    }

    @Override
    public boolean swithToBranch(String branch) {
        return delegate.swithToBranch(branch);
    }
    
    /**
     * Returns the path of the handled repository.
     * @return The path of the repository or <tt>basePath</tt> if no repository was checked out so far.
     */
    public File getRepoPath() {
        return basePath;
    }
}
