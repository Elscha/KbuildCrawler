package net.ssehub.kBuildCrawler.metrics;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.ssehub.kernel_haven.metric_haven.multi_results.MeasuredItem;
import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;

public class MultiMetricJoinerTest {

    @Test
    public void testJoinFunctionAndCompleteMetricResults() {
        MultiMetricResult full1 = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {13.2, -34.2}
        );
        MultiMetricResult full2 = new MultiMetricResult(new MeasuredItem("some/file.c", 654, "func2"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {435.2, 213.2}
        );
        MultiMetricResult full3 = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func3"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {34.2, 23.2}
        );
        
        List<MultiMetricResult> completeTree = Arrays.asList(full1, full2, full3);
        
        MultiMetricResult single1 = new MultiMetricResult(new MeasuredItem("some/file.c", 654, "func2"),
                new String[] {"McCabe", "Inner Variability", "Outer Variability"},
                new Double[] {534.3, 123.4, 3432.2}
        );
        MultiMetricResult single2 = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"McCabe", "Inner Variability", "Outer Variability"},
                new Double[] {34.3, 443.2, 54.3}
        );
        
        List<MultiMetricResult> functionMetrics = Arrays.asList(single1, single2);
        
        List<MultiMetricResult> result = KernelHavenRunner.joinFunctionAndCompleteMetricResults(completeTree, functionMetrics);
        
        assertEquals(2, result.size());
        assertArrayEquals(
                new String[] {"Source File", "Line No.", "Element", "McCabe", "Inner Variability", "Outer Variability", "Fan In", "Fan Out"},
                result.get(0).getHeader());
        assertArrayEquals(
                new String[] {"Source File", "Line No.", "Element", "McCabe", "Inner Variability", "Outer Variability", "Fan In", "Fan Out"},
                result.get(1).getHeader());
        
        assertArrayEquals(
                new Object[] {"some/file.c", 14, "func1", 34.3, 443.2, 54.3, 13.2, -34.2},
                result.get(0).getContent());
        assertArrayEquals(
                new Object[] {"some/file.c", 654, "func2", 534.3, 123.4, 3432.2, 435.2, 213.2},
                result.get(1).getContent());
    }
    
    @Test
    public void testJoinFullMetricResults_SingleFunctionInBothLists() {
        MultiMetricResult l11 = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {13.2, -34.2}
        );
        MultiMetricResult l21 = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"McCabe"},
                new Double[] {12d}
        );
        
        List<MultiMetricResult> result = KernelHavenRunner.joinFullMetricResults(Arrays.asList(l11), Arrays.asList(l21));
        
        assertEquals(1, result.size());
        
        assertArrayEquals(
                new String[] {"Source File", "Line No.", "Element", "Fan In", "Fan Out", "McCabe"},
                result.get(0).getHeader());
        
        assertArrayEquals(
                new Object[] {"some/file.c", 14, "func1", 13.2, -34.2, 12d},
                result.get(0).getContent());
    }
    
    @Test
    public void testJoinFullMetricResults_OneDifferentFunctionInBothLists() {
        MultiMetricResult l11 = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {13.2, -34.2}
        );
        MultiMetricResult l21 = new MultiMetricResult(new MeasuredItem("some/file.c", 100, "func2"),
                new String[] {"McCabe"},
                new Double[] {12d}
        );
        
        List<MultiMetricResult> result = KernelHavenRunner.joinFullMetricResults(Arrays.asList(l11), Arrays.asList(l21));
        
        assertEquals(2, result.size());
        
        assertArrayEquals(
                new String[] {"Source File", "Line No.", "Element", "Fan In", "Fan Out", "McCabe"},
                result.get(0).getHeader());
        
        assertArrayEquals(
                new Object[] {"some/file.c", 14, "func1", 13.2, -34.2, null},
                result.get(0).getContent());
        assertArrayEquals(
                new Object[] {"some/file.c", 100, "func2", null, null, 12d},
                result.get(1).getContent());
    }
    
}
