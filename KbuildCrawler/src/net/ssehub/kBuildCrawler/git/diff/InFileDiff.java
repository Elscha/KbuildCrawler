package net.ssehub.kBuildCrawler.git.diff;

/**
 * Represents one block of added and/or removed lines
 * @author El-Sharkawy
 *
 */
public class InFileDiff {
    private int startLineBefore;
    private int nLinesBefore;
    private int startLineAfter;
    private int nLinesAfter;
    
    InFileDiff(int startLineBefore, int nLinesBefore, int startLineAfter, int nLinesAfter) {
        this.startLineBefore = startLineBefore;
        this.nLinesBefore = nLinesBefore;
        this.startLineAfter = startLineAfter;
        this.nLinesAfter = nLinesAfter;
    }

    public int getStartLineBefore() {
        return startLineBefore;
    }

    public int getNumberOfChangesLinesBefore() {
        return nLinesBefore;
    }

    public int getStartLineAfter() {
        return startLineAfter;
    }

    public int getNumberOfChangesLinesAfter() {
        return nLinesAfter;
    }

}
