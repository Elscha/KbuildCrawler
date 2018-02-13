package net.ssehub.kBuildCrawler.git.plugins;

import java.io.File;

import net.ssehub.kBuildCrawler.git.GitData;

/**
 * High level operations, which can be realized through the basis operations of the {@link IGitPlugin}.
 * @author El-Sharkawy
 *
 */
abstract class AbstractGitPlugin implements IGitPlugin {
    protected static final String DEFAULT_BRANCH = "master";
    
    @Override
    public File restoreCommit(GitData commitInfo) {
        File folder = null;
        if (commitInfo.cloningPossible()) {
            // Clone repository
            String branch = null != commitInfo.getBranch() ? commitInfo.getBranch() : DEFAULT_BRANCH;
            folder = clone(commitInfo.getBase(), branch);
            setBasePath(folder);
            
            // Checkout desired commit
            boolean successful = checkout(commitInfo.getCommit());
            if (!successful) {
                folder = null;
            }
        } else {
            System.err.println("Currently not supported git info: " + commitInfo);
        }
        
        return folder;
    }

}
