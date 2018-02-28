package net.ssehub.kBuildCrawler.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * Represents a local git repository directory.
 * 
 * @author Adam
 */
public class GitRepository {

    private static final boolean DEBUG_LOGGING = true;
    
    private static final Logger LOGGER = Logger.get();
    
    private File workingDirectory;
    
    /**
     * Creates a {@link GitRepository} for the given folder.
     * 
     * @param workingDirectory The working directory. If it doesn't exist yet, it will be created.
     * 
     * @throws GitException If workingDirectory is not a git repository and it cannot be initialized as one.
     */
    public GitRepository(File workingDirectory) throws GitException {
        this.workingDirectory = workingDirectory;
        if (!workingDirectory.isDirectory()) {
            workingDirectory.mkdir();
        }
        
        if (!workingDirectory.isDirectory()) {
            throw new GitException(workingDirectory + " is not a directory");
        }
        
        if (!new File(workingDirectory, ".git").isDirectory()) {
            init();
        }
    }
    
    /**
     * Initializes this git repository. Calls <code>git init</code>.
     */
    private void init() throws GitException {
        runGitCommand("git", "init");
    }
    
    /**
     * Adds a remote to this git repository.
     * 
     * @throws GitException If adding the remote fails.
     */
    public void addRemote(String name, String url) throws GitException {
        runGitCommand("git", "remote", "add", name, url);
    }
    
    /**
     * Gets a set of all remote names that have been added to this git repository.
     * 
     * @return A set of all remote names.
     * 
     * @throws GitException 
     */
    public Set<String> getRemotes() throws GitException {
        String output = runGitCommand("git", "remote");
        
        Set<String> result = new HashSet<>();
        
        for (String line : output.split("\\n")) {
            result.add(line);
        }
        
        return result;
    }
    
    /**
     * Creates a remote name for the given URL.
     * 
     * @param url The URL to create a remote name for.
     * 
     * @return A remote name.
     */
    public static String createRemoteName(String url) {
        // drop leading protocol
        int colonIndex = url.indexOf(':');
        if (colonIndex != -1) {
            url = url.substring(colonIndex + 1);
            while (url.startsWith("/")) {
                url = url.substring(1);
            }
        }
        
        // drop trailing .git
        if (url.endsWith(".git")) {
            url = url.substring(0, url.length() - ".git".length());
        }
        
        // replace everything that isn't alphanumeric
        return url.replaceAll("[^A-Za-z0-9_\\-]", "_");
    }
    
    /**
     * Fetches the given remote.
     * 
     * @param remoteName The remote to fetch.
     * 
     * @throws GitException If fetching fails.
     */
    public void fetch(String remoteName) throws GitException {
        runGitCommand("git", "fetch", remoteName);
    }
    
    /**
     * Checks out the given commit.
     * 
     * @param commitHash The commit to check out. May also be a branch or tag name.
     */
    public void checkout(String commitHash) throws GitException {
        runGitCommand("git", "checkout", "--force", commitHash);
    }
    
    /**
     * Checks out the given branch on the given remote.
     * 
     * @param remoteName The name of the remote that has the branch.
     * @param branch The name of the branch in the remote to check out.
     */
    public void checkout(String remoteName, String branch) throws GitException {
        runGitCommand("git", "checkout", "--force", remoteName + "/" + branch);
    }
    
    /**
     * Returns the commit hash that is directly before <code>date</code> in the given <code>branch</code>.
     * 
     * @param remoteName The name of the remote that has the given branch.
     * @param branch The branch to get the commit hash for.
     * @param date The date of the commit. The commit closest to and before this date will be returned.
     * 
     * @return The commit hash.
     */
    public String getCommitBefore(String remoteName, String branch, String date) throws GitException {
        String hash = runGitCommand("git", "rev-list", "-n", "1", "--before=" + date, remoteName + "/" + branch);
        
        return hash;
    }
    
    /**
     * The working directory of this git repository.
     * 
     * @return The working directory. This is an existing folder.
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }
    
    /**
     * Runs the given git command.
     * 
     * @param command The command to run, with command line parameters.
     * 
     * @return The standard output stream content.
     */
    private String runGitCommand(String... command) throws GitException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(workingDirectory);
        
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        
        boolean success = false;
        
        if (DEBUG_LOGGING) {
            LOGGER.logDebug(Arrays.toString(command));
        }
        
        try {
            success = Util.executeProcess(builder, "Git", stdout, stderr, 0);
            
        } catch (IOException e) {
            throw new GitException(e);
            
        } finally {
            if (DEBUG_LOGGING) {
                LOGGER.logDebug("Stdout:", stdout.toString().trim());
                LOGGER.logDebug("Stderr:", stderr.toString().trim());
            }
        }
        
        if (!success) {
            throw new GitException(stderr.toString().trim());
        }
        
        return stdout.toString().trim();
    }
    
}
