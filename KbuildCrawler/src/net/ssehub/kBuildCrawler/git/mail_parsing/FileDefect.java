package net.ssehub.kBuildCrawler.git.mail_parsing;

import java.util.Objects;

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
        VULNERABILITY,
        UNKNOWN
    }

    public static final int UNKNOWN_POSITION = -1;
    
    private String path;
    private String file;
    private String function;
    private int line;
    private int posStart;
    private String description;
    private Type type;
    
    /**
     * Constructor for a file based diff (via <b>mails</b>).
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
        this.function = null;
    }
    
    /**
     * Constructor for a file based diff (via <b>CSV list of CVEs</b>).
     * @param path The relative path to the file inside the repository, without the file itself
     * @param file The file (last segment of the path).
     * @param line The reported starting line of the defect.
     * @param posStart The reported starting positing within the line
     * @param description Optional: A description of the reported failure.
     */
    public FileDefect(String path, String file, String function, String cve, String type) {
        this.path = path;
        this.file = file;
        this.line = UNKNOWN_POSITION;
        this.posStart = UNKNOWN_POSITION;
        this.description = cve;
        this.type = determineType(type);
        this.function = function;
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
            
        } else if (description.startsWith("vulnerability")) {
            result = Type.VULNERABILITY;
            
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
    
    /**
     * Returns the identified, affected code function.
     * @return Will be <tt>null</tt> in case of mailing list-based API is used, otherwise the reported code function.
     */
    public String getFunction() {
        return function;
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

    @Override
    public int hashCode() {
        return Objects.hash(description, file, function, line, path, posStart, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FileDefect)) {
            return false;
        }
        FileDefect other = (FileDefect) obj;
        return Objects.equals(description, other.description) && Objects.equals(file, other.file)
                && Objects.equals(function, other.function) && line == other.line && Objects.equals(path, other.path)
                && posStart == other.posStart && type == other.type;
    }
    
    
}
