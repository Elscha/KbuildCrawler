package net.ssehub.kBuildCrawler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect;
import net.ssehub.kBuildCrawler.git.mail_parsing.FileDefect.Type;
import net.ssehub.kernel_haven.metric_haven.multi_results.MeasuredItem;
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;

/**
 * Tests parts of the {@link KbuildCrawler} class.
 * 
 * @author Adam
 */
public class KbuildCrawlerTest {

    /**
     * Tests a simple case for {@link KbuildCrawler#determineTypes(java.util.List, java.util.List)}.
     */
    @Test
    public void testDetermineTypesOneFile() {
        List<MultiMetricResult> mmrs = new LinkedList<>();
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 1, "f1"), new String[0], new Double[0]));
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 5, "f2"), new String[0], new Double[0]));
        
        List<FileDefect> defects = new LinkedList<>();
        defects.add(new FileDefect("dir/", "file.c", 2, 0, "warning: some warning"));
        defects.add(new FileDefect("dir/", "file.c", 6, 0, "error: some error"));
        
        List<Type> result = KbuildCrawler.determineTypes(mmrs, defects);
        
        assertThat(result, is(Arrays.asList(Type.WARNING, Type.ERROR)));
    }
    
    /**
     * Tests that multiple markers in the same function are handled properly.
     */
    @Test
    public void testDetermineTypesMultipleMarkersInFunction() {
        List<MultiMetricResult> mmrs = new LinkedList<>();
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 1, "f1"), new String[0], new Double[0]));
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 5, "f2"), new String[0], new Double[0]));
        
        List<FileDefect> defects = new LinkedList<>();
        defects.add(new FileDefect("dir/", "file.c", 2, 0, "warning: some warning"));
        defects.add(new FileDefect("dir/", "file.c", 3, 0, "note: some note"));
        defects.add(new FileDefect("dir/", "file.c", 4, 0, "sparse: some sparse warning"));
        defects.add(new FileDefect("dir/", "file.c", 6, 0, "error: some error"));
        
        List<Type> result = KbuildCrawler.determineTypes(mmrs, defects);
        
        assertThat(result, is(Arrays.asList(Type.SPARSE, Type.ERROR)));
    }
    
    /**
     * Tests that multiple files are handled properly.
     */
    @Test
    public void testDetermineTypesMultipleFiles() {
        List<MultiMetricResult> mmrs = new LinkedList<>();
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 1, "f1"), new String[0], new Double[0]));
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 5, "f2"), new String[0], new Double[0]));
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir2/file.c", 1, "f3"), new String[0], new Double[0]));
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file2.c", 5, "f4"), new String[0], new Double[0]));
        
        List<FileDefect> defects = new LinkedList<>();
        defects.add(new FileDefect("dir2/", "file.c", 2, 0, "error: some error"));
        defects.add(new FileDefect("dir/", "file.c", 2, 0, "warning: some warning"));
        defects.add(new FileDefect("dir/", "file2.c", 6, 0, "sparse: some sparse"));
        defects.add(new FileDefect("dir/", "file.c", 6, 0, "note: some note"));
        
        List<Type> result = KbuildCrawler.determineTypes(mmrs, defects);
        
        assertThat(result, is(Arrays.asList(Type.WARNING, Type.NOTE, Type.ERROR, Type.SPARSE)));
    }
    
    /**
     * Tests that multiple following functions are handled properly.
     */
    @Test
    public void testDetermineTypesMultipleFollowingFunctions() {
        List<MultiMetricResult> mmrs = new LinkedList<>();
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 1, "f1"), new String[0], new Double[0]));
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 10, "f3"), new String[0], new Double[0]));
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 5, "f2"), new String[0], new Double[0]));
        
        List<FileDefect> defects = new LinkedList<>();
        defects.add(new FileDefect("dir/", "file.c", 20, 0, "error: some error"));
        defects.add(new FileDefect("dir/", "file.c", 2, 0, "note: some note"));
        defects.add(new FileDefect("dir/", "file.c", 6, 0, "warning: some warning"));
        
        List<Type> result = KbuildCrawler.determineTypes(mmrs, defects);
        
        assertThat(result, is(Arrays.asList(Type.NOTE, Type.ERROR, Type.WARNING)));
    }
    
    /**
     * Tests that a function without a defect is correctly handled.
     */
    @Test
    public void testMissingDefect() {
        List<MultiMetricResult> mmrs = new LinkedList<>();
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 1, "f1"), new String[0], new Double[0]));
        mmrs.add(new MultiMetricResult(new MeasuredItem("dir/file.c", 5, "f2"), new String[0], new Double[0]));
        
        List<FileDefect> defects = new LinkedList<>();
        defects.add(new FileDefect("dir/", "file.c", 2, 0, "warning: some warning"));
        
        List<Type> result = KbuildCrawler.determineTypes(mmrs, defects);
        
        assertThat(result, is(Arrays.asList(Type.WARNING, Type.UNKNOWN)));
    }
    
}
