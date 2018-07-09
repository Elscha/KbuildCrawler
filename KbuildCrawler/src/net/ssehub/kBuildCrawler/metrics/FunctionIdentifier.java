package net.ssehub.kBuildCrawler.metrics;

import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;

class FunctionIdentifier {
    
    static boolean HAS_INCLUDED_FILE = false;
    
    private static final int FILE_INDEX = 0;
    private static final int LINE_NUMBER_INDEX = 1;
    private static final int ELEMNET_INDEX = 2;
    
    private String file;
    private String lineNumber;
    private String element;
    
    public FunctionIdentifier(Object file, Object lineNumber, Object element) {
        this.file = file == null ? "" : file.toString();
        this.lineNumber = lineNumber == null ? "" : lineNumber.toString();
        this.element = element == null ? "" : element.toString();
    }
    
    public FunctionIdentifier(MultiMetricResult multiMetricResult) {
        this(multiMetricResult.getContent()[getFileIndex()], multiMetricResult.getContent()[getLineNumberIndex()],
                multiMetricResult.getContent()[getElementIndex()]);
    }
    
    private static int getFileIndex() {
        return FILE_INDEX;
    }
    
    private static int getLineNumberIndex() {
        return HAS_INCLUDED_FILE ? LINE_NUMBER_INDEX + 1 : LINE_NUMBER_INDEX;
    }
    
    private static int getElementIndex() {
        return HAS_INCLUDED_FILE ? ELEMNET_INDEX + 1 : ELEMNET_INDEX;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj instanceof FunctionIdentifier) {
            FunctionIdentifier other = (FunctionIdentifier) obj;
            equal = this.file.equals(other.file);
            equal = this.lineNumber.equals(other.lineNumber);
            equal = this.element.equals(other.element);
        }
        return equal;
    }
    
    @Override
    public int hashCode() {
        return file.hashCode() + lineNumber.hashCode() + element.hashCode();
    }
    
}