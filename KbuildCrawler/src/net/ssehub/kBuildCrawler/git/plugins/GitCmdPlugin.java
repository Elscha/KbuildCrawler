package net.ssehub.kBuildCrawler.git.plugins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ssehub.kBuildCrawler.io.ConsoleOutputHandler;
import net.ssehub.kBuildCrawler.io.IProcessOutputHandler;
import net.ssehub.kBuildCrawler.io.InputReader;

/**
 * {@link IGitPlugin} which uses a locally installed git binary for the specified operations.
 * @author El-Sharkawy
 *
 */
public class GitCmdPlugin extends AbstractGitPlugin {
    
    private final static String GIT_CMD = "git";
    
    private File basePath;
    private File gitPath;
    
    /**
     * Single constructor for this class.
     * @param path Optional: Folder where to find the <tt>git</tt> binary. Maybe <tt>null</tt> if the folder is part
     * of the <tt>PATH</tt> variable.
     */
    public GitCmdPlugin(File path) {
        this.gitPath = path;
        basePath = null;
    }

    @Override
    public File setBasePath(File basePath) {
        boolean validFolder = false;
        if (!basePath.exists()) {
            validFolder = basePath.mkdirs();
        } else {
            validFolder = basePath.isDirectory();
        }
        
        if (validFolder) {
            this.basePath = basePath;
        }
        
        return this.basePath;
    }

    /**
     * Executes a git command at the command line.
     * @param folder The folder where to perform the operation (e.g. where to download a repository).
     * @param commands The commands to execute ad the command line.
     * @param handler An output handle for the output streams of a {@link Process}.
     * @return <tt>true</tt> if update (fetch) was successful, <tt>false</tt> if not.
     */
    private boolean executeGitCommand(File folder, List<String> commands, IProcessOutputHandler handler) {
        boolean success = false;
        ProcessBuilder pb = new ProcessBuilder(commands);
        if (null != folder) {
            pb.directory(folder);
        }
        try {
            Process process = pb.start();
            if (null != handler) {
                handler.gobble(process);                
            }
            int returnCode = process.waitFor();
            success = 0 == returnCode;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return success;
    }
    
    /**
     * Creates <tt>git</tt> command and considers the specified location of the git executable.
     * @param gitCommand The name of the git executable.
     * @return <tt>gitCommand</tt> or <tt>basePath/gitCommand</tt>.
     */
    private String createGitCommand(String gitCommand) {
        String cmd = gitCommand;
        if (null != gitPath) {
            cmd = new File(gitPath, gitCommand).getAbsolutePath();
        }
        
        return cmd;
    }
    
    /**
     * Creates <tt>git</tt> command and considers the specified location of the git executable.
     * @return <tt>{@value GitCmdPlugin#GIT_CMD}</tt> or <tt>basePath/{@value GitCmdPlugin#GIT_CMD}</tt>.
     */
    private String createGitCommand() {
        return createGitCommand(GIT_CMD);
    }
    
    @Override
    public File clone(String url, String branch) {
        File newRepoPath = null;
        List<String> commands = new ArrayList<>();
        
        // Git clone command
        commands.add(createGitCommand());
        commands.add("clone");
        
        // Optional branch
        if (null != branch) {
            commands.add("--branch");
            commands.add(branch);
        };
        
        // Clone url
        commands.add(url);
        
        // Existing files & folders before operation
        Set<File> fileSet = new HashSet<>();
        File[] files = basePath.listFiles();
        if (null != files) {
            fileSet.addAll(Arrays.asList(files));
        }
        
        boolean success = executeGitCommand(basePath, commands, new ConsoleOutputHandler());
        
        if (success) {
            files = basePath.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length && null == newRepoPath; i++) {
                    if (!fileSet.contains(files[i])) {
                        newRepoPath = files[i];
                    }
                }
            }
        }
        
        return newRepoPath;
    }

    @Override
    public String diff(String headSHA1, String commitSHA1) {
        List<String> commands = new ArrayList<>();
        
        // Git diff command
        commands.add(createGitCommand());
        commands.add("diff");
        
        // Both revisions
        commands.add(headSHA1);
        commands.add(commitSHA1);
        
        InputReader reader = new InputReader();
        boolean success = executeGitCommand(basePath, commands, reader);
        
        return success ? reader.getOutput() : null;
    }

    @Override
    public boolean checkout(String hash) {
        List<String> commands = new ArrayList<>();
        
        // Git checkout command
        commands.add(createGitCommand());
        commands.add("checkout");
        
        // desired revision
        commands.add(hash);
        
        InputReader reader = new InputReader(false);
        boolean success = executeGitCommand(basePath, commands, reader);

        if (success) {
            String output = reader.getOutput();
            boolean containsStart = output.contains("HEAD is now at ");
            int start = "HEAD is now at ".length();
            int end = start + 7;
            if (containsStart && start < output.length() && end <= output.length()) {
                String extractedSHA = output.substring(start, end);
                success = hash.startsWith(extractedSHA);
            }
        }
        
        return success;
    }

    @Override
    public boolean fetch() {
        List<String> commands = new ArrayList<>();
        
        // Git fetch command
        commands.add(createGitCommand());
        commands.add("fetch");
        
        InputReader reader = new InputReader(false);
        boolean success = executeGitCommand(basePath, commands, reader);

        if (success) {
            String output = reader.getOutput();
            success = output.isEmpty();
        }
        
        return success;
    }

    @Override
    public boolean swithToBranch(String branch) {
        List<String> commands = new ArrayList<>();
        
        // Git checkout command
        commands.add(createGitCommand());
        commands.add("checkout");
        
        // desired branch
        commands.add(branch);
        
        InputReader reader = new InputReader(true);
        boolean success = executeGitCommand(basePath, commands, reader);

        if (success) {
            String output = reader.getOutput();
            success = output.contains("Your branch is up-to-date with 'origin/" + branch + "'.");
        }
        
        return success;
    }
}
