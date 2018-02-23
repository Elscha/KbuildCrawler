package net.ssehub.kBuildCrawler.git;

/**
 * An exception thrown by {@link GitRepository}.
 * 
 * @author Adam
 */
public class GitException extends Exception {

    private static final long serialVersionUID = -2230086275703944780L;
    
    public GitException() {
    }
    
    public GitException(String message) {
        super(message);
    }
    
    public GitException(Throwable cause) {
        super(cause);
    }
    
    public GitException(String message, Throwable cause) {
        super(message, cause);
    }

}
