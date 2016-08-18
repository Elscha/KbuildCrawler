package net.ssehub.kBuildCrawler.git;

public class FileDefect {
    private String path;
    private String file;
    private int line;
    private int posStart;
    private String description;
    
    FileDefect(String path, String file, int line, int posStart, String description) {
        this.path = path;
        this.file = file;
        this.line = line;
        this.posStart = posStart;
        this.description = description;
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
