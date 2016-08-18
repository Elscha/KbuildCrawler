package net.ssehub.kBuildCrawler.git;

import java.io.File;

public interface IGitPlugin {
    public File setBasePath(File basePath);
    public File clone(String url, String branch);
    public String diff(String headSHA1, String commitSHA1);
    public boolean checkout(String hash);
    public boolean fetch();
    public boolean swithToBranch(String branch);
}
