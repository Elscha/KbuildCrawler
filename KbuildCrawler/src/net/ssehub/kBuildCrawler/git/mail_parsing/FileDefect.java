package net.ssehub.kBuildCrawler.git.mail_parsing;

import net.ssehub.kernel_haven.util.Logger;

/**
 * Points to a single failure reported in a {@link net.ssehub.kBuildCrawler.mail.Mail}.
 * @author El-Sharkawy
 *
 */
public class FileDefect {
    
    public static enum Type {
        ERROR,
        SPARSE, // this is a static analysis tool specifically for the Linux kernel
        WARNING,
        NOTE,
        UNKNOWN
    }
    
    private String path;
    private String file;
    private int line;
    private int posStart;
    private String description;
    private Type type;
    
    /**
     * Sole constructor for a file based diff.
     * @param path The relative path to the file inside the repository, without the file itself
     * @param file The file (last segment of the path).
     * @param line The reported starting line of the defect.
     * @param posStart The reported starting positing within the line
     * @param description Optional: A description of the reported failure.
     */
    public FileDefect(String path, String file, int line, int posStart, String description) {
        this.path = path;
        this.file = file;
        this.line = line;
        this.posStart = posStart;
        this.description = description;
        this.type = determineType(description);
    }
    
    private static Type determineType(String description) {
        description = description.toLowerCase().trim();
        
        Type result;
        
        if (description.startsWith("error:") || description.startsWith("fatal error:")) {
            result = Type.ERROR;
            
        } else if (description.startsWith("warning:")) {
            result = Type.WARNING;
            
        } else if (description.startsWith("note:")) {
            result = Type.NOTE;
            
        } else if (description.startsWith("sparse:")) {
            result = Type.SPARSE;
            
        } else {
            Logger.get().logWarning("Could not determine type for: " + description, "Using UNKNOWN");
            result = Type.UNKNOWN;
        }
        
        return result;
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
    
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        String error = path + file + ":" + line + ":" + posStart + " " + type;
        if (null != description) {
            error += " = " + description;
        }
        
        return error;
    }
    
}
