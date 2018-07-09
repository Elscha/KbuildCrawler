package net.ssehub.kBuildCrawler.metrics;

public class KernelHavenRunnerFactory {
    
    public static AbstractKernelHavenRunner createRunner(boolean asProcess) {
        AbstractKernelHavenRunner runner;
        if (asProcess) {
            runner = new KernelHavenProcessRunner();
        } else {
            runner = new KernelHavenRunner();
        }
        
        return runner;
    }

}
