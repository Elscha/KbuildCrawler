package net.ssehub.kBuildCrawler.metrics;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.ssehub.kernel_haven.metric_haven.multi_results.MultiMetricResult;

public class MultiMetricJoinerTest {

    @Test
    public void testMetricJoin() throws IOException {
        MultiMetricResult full1 = new MultiMetricResult(
                new String[] {"Source File", "Line No.", "Element", "Fan In", "Fan Out"},
                new Object[] {"some/file.c", 14, "func1", 13.2, -34.2}
        );
        MultiMetricResult full2 = new MultiMetricResult(
                new String[] {"Source File", "Line No.", "Element", "Fan In", "Fan Out"},
                new Object[] {"some/file.c", 654, "func2", 435.2, 213.2}
        );
        MultiMetricResult full3 = new MultiMetricResult(
                new String[] {"Source File", "Line No.", "Element", "Fan In", "Fan Out"},
                new Object[] {"other/file.c", 14, "func3", 34.2, 23.2}
        );
        
        List<MultiMetricResult> completeTree = Arrays.asList(full1, full2, full3);
        
        
        MultiMetricResult single1 = new MultiMetricResult(
                new String[] {"Source File", "Line No.", "Element", "McCabe", "Inner Variability", "Outer Variability"},
                new Object[] {"some/file.c", 654, "func2", 534.3, 123.4, 3432.2}
        );
        MultiMetricResult single2 = new MultiMetricResult(
                new String[] {"Source File", "Line No.", "Element", "McCabe", "Inner Variability", "Outer Variability"},
                new Object[] {"some/file.c", 14, "func1", 34.3, 443.2, 54.3}
        );
        
        List<List<MultiMetricResult>> functionMetrics = Arrays.asList(Arrays.asList(single1), Arrays.asList(single2));
        
        List<MultiMetricResult> result = KernelHavenRunner.joinMultiMetricResults(completeTree, functionMetrics);
        
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
    
}
