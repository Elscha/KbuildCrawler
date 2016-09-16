package net.ssehub.kBuildCrawler.io;

/**
 * Defines an output handle for the output streams of a {@link Process}.
 * @author El-Sharkawy
 *
 */
public interface IProcessOutputHandler {
    
    /**
     * Handles the two streams of the given process.
     * @param process The process to be executed.
     */
    public void gobble(Process process);

}
