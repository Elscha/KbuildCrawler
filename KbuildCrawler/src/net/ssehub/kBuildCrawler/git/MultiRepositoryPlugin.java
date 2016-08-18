package net.ssehub.kBuildCrawler.git;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MultiRepositoryPlugin implements IGitPlugin {
    
    private File basePath;
    private Map<String, StateFullGitPlugin> repositories;
    private StateFullGitPlugin currentRepoPlugin;
    
    public MultiRepositoryPlugin(File basePath) {
        this.basePath = basePath;
        repositories = new HashMap<>();
        currentRepoPlugin = null;
    }
    
    private void setRepositoryPlugin(String repoURL) {
        currentRepoPlugin = repositories.get(repoURL);
        if (null == currentRepoPlugin) {
            currentRepoPlugin = new StateFullGitPlugin(new GitCmdPlugin(null), basePath);
            repositories.put(repoURL, currentRepoPlugin);
        }
    }

    @Override
    public File setBasePath(File basePath) {
        // switching base path is not supported by this plugin
        File currentFolder = null != currentRepoPlugin? currentRepoPlugin.getRepoPath() : basePath;
        return currentFolder;
    }

    @Override
    public File clone(String url, String branch) {
        setRepositoryPlugin(url);
        return currentRepoPlugin.clone(url, branch);
    }

    @Override
    public String diff(String headSHA1, String commitSHA1) {
        return currentRepoPlugin.diff(headSHA1, commitSHA1);
    }

    @Override
    public boolean checkout(String hash) {
        return currentRepoPlugin.checkout(hash);
    }

    @Override
    public boolean fetch() {
        return currentRepoPlugin.fetch();
    }

    @Override
    public boolean swithToBranch(String branch) {
        return currentRepoPlugin.swithToBranch(branch);
    }
}
