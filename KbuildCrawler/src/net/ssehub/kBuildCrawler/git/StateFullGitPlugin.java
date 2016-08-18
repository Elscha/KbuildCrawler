package net.ssehub.kBuildCrawler.git;

import java.io.File;

/**
 * Implements a git plugin, which stores the state to avoid multiple checkouts of the same repository.
 * @author El-Sharkawy
 *
 */
public class StateFullGitPlugin implements IGitPlugin {

    private IGitPlugin delegate;
    private File basePath;
    private String remoteURL;
    
    /**
     * Single constructor for this class.
     * @param plugin Another git plugin which is used by this plugin (decorator pattern).
     * @param basePath The base path, where to checkout a newly downloaded repository (parent folder).
     */
    public StateFullGitPlugin(IGitPlugin plugin, File basePath) {
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
                if (null != branch) {
                    delegate.swithToBranch(branch);
                } else {
                    delegate.swithToBranch("master");
                }
                delegate.fetch();
            } else {
                basePath = delegate.clone(url, branch);                
            }
            
            if (oldPath != basePath && basePath.exists()) {
                remoteURL = url;
                delegate.setBasePath(basePath);
            }
        }
        
        return basePath;
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
     * Returns the path of the handles repository.
     * @return The path of the repository or <tt>basePath</tt> if no repository was checked out so far.
     */
    public File getRepoPath() {
        return basePath;
    }
}
