package net.ssehub.kBuildCrawler.io;

public class ConsoleOutputHandler implements IProcessOutputHandler {

    @Override
    public void gobble(Process process) {
        StreamGobbler.gobble(process);
    }

}
