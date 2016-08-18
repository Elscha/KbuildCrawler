package net.ssehub.kBuildCrawler.git.diff;

public class FileDiff {
    
    private ChangeType fileChange;
    private String file;

    FileDiff(String file, ChangeType fileChange) {
        this.file = file;
        this.fileChange = fileChange;
    }

    public String getFile() {
        return file;
    }

    public ChangeType getFileChange() {
        return fileChange;
    }
}
