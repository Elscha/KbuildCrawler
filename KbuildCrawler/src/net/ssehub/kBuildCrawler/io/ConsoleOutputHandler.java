package net.ssehub.kBuildCrawler.io;

/**
 * Uses two {@link StreamGobbler}s to redirect the output to {@link System#out} and {@link System#err}.
 * @author El-Sharkawy
 *
 */
public class ConsoleOutputHandler implements IProcessOutputHandler {

    @Override
    public void gobble(Process process) {
        StreamGobbler.gobble(process);
    }

}
