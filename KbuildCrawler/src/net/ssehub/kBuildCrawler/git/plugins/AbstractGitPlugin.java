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
    public boolean restoreCommit(GitData commitInfo) {
        boolean successful = false;
        if (commitInfo.cloningPossible()) {
            // Clone repository
            String branch = null != commitInfo.getBranch() ? commitInfo.getBranch() : DEFAULT_BRANCH;
            File folder = clone(commitInfo.getBase(), branch);
            setBasePath(folder);
            
            // Checkout desired commit
            successful = checkout(commitInfo.getCommit());
        } else {
            System.err.println("Currently not supported git info: " + commitInfo);
        }
        
        return successful;
    }

}
