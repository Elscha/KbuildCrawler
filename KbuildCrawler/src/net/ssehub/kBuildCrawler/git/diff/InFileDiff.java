package net.ssehub.kBuildCrawler.git.diff;

/**
 * Represents one block (hunk) of added and/or removed lines
 * @author El-Sharkawy
 *
 */
public class InFileDiff {
    private int startLineBefore;
    private int nLinesBefore;
    private int startLineAfter;
    private int nLinesAfter;
    
    /**
     * Sole constructor for this class.
     * @param startLineBefore The line number of the changed block, <b>before</b> the change was implemented.
     * @param nLinesBefore How many lines belong to the block, <b>before</b> the change was implemented.
     * @param startLineAfter The line number of the changed block, <b>after</b> the change was implemented.
     * @param nLinesAfter How many lines belongs to the block, <b>after</b> the change was implemented.
     */
    InFileDiff(int startLineBefore, int nLinesBefore, int startLineAfter, int nLinesAfter) {
        this.startLineBefore = startLineBefore;
        this.nLinesBefore = nLinesBefore;
        this.startLineAfter = startLineAfter;
        this.nLinesAfter = nLinesAfter;
    }

    /**
     * The line number of the changed block, <b>before</b> the change was implemented.
     * @return The line number of the changed block, <b>before</b> the change was implemented.
     */
    public int getStartLineBefore() {
        return startLineBefore;
    }

    /**
     * How many lines belong to the block, <b>before</b> the change was implemented.
     * @return How many lines belong to the block, <b>before</b> the change was implemented.
     */
    public int getNumberOfChangesLinesBefore() {
        return nLinesBefore;
    }

    /**
     * The line number of the changed block, <b>after</b> the change was implemented.
     * @return  The line number of the changed block, <b>after</b> the change was implemented.
     */
    public int getStartLineAfter() {
        return startLineAfter;
    }

    /**
     * How many lines belongs to the block, <b>after</b> the change was implemented.
     * @return How many lines belongs to the block, <b>after</b> the change was implemented.
     */
    public int getNumberOfChangesLinesAfter() {
        return nLinesAfter;
    }

}
