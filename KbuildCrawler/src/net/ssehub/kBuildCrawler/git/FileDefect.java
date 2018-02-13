package net.ssehub.kBuildCrawler.git;

/**
 * Points to a single failure reported in a {@link net.ssehub.kBuildCrawler.mail.Mail}.
 * @author El-Sharkawy
 *
 */
public class FileDefect {
    private String path;
    private String file;
    private int line;
    private int posStart;
    private String description;
    
    /**
     * Sole constructor for a file based diff.
     * @param path The relative path to the file inside the repository, without the file itself
     * @param file The file (last segment of the path).
     * @param line The reported starting line of the defect.
     * @param posStart The reported starting positing within the line
     * @param description Optional: A description of the reported failure.
     */
    FileDefect(String path, String file, int line, int posStart, String description) {
        this.path = path;
        this.file = file;
        this.line = line;
        this.posStart = posStart;
        this.description = description;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getFile() {
        return file;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getPosStart() {
        return posStart;
    }
    
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        String error = path + file + ":" + line + ":" + posStart;
        if (null != description) {
            error += " = " + description;
        }
        
        return error;
    }
    
}
