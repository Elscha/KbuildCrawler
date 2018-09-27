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
    public void testJoinFullMetricResults_SingleFunctionInBothLists() {
        MultiMetricResult l11 = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {13.2, -34.2}
        );
        MultiMetricResult l21 = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"McCabe"},
                new Double[] {12d}
        );
        
        List<MultiMetricResult> result = AbstractKernelHavenRunner.joinFullMetricResults(Arrays.asList(l11), Arrays.asList(l21));
        
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
        
        List<MultiMetricResult> result = AbstractKernelHavenRunner.joinFullMetricResults(Arrays.asList(l11), Arrays.asList(l21));
        
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
    
    @Test
    public void testTryFixToFormat_CorrectMetrics() {
        MultiMetricResult mmr = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {13.2, -34.2}
        );
        String[] expecteMetrics = {"Fan In", "Fan Out"};
        
        MultiMetricResult fixed = AbstractKernelHavenRunner.tryFixToFormat(expecteMetrics, mmr);
        
        assertArrayEquals(
                new String[] {"Source File", "Line No.", "Element", "Fan In", "Fan Out"},
                fixed.getHeader());
        
        assertArrayEquals(
                new Object[] {"some/file.c", 14, "func1", 13.2, -34.2},
                fixed.getContent());
    }
    
    @Test
    public void testTryFixToFormat_MissingValue() {
        MultiMetricResult mmr = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {13.2, -34.2}
        );
        String[] expecteMetrics = {"Fan In", "Fan Out", "McCabe"};
        
        MultiMetricResult fixed = AbstractKernelHavenRunner.tryFixToFormat(expecteMetrics, mmr);
        
        assertArrayEquals(
                new String[] {"Source File", "Line No.", "Element", "Fan In", "Fan Out", "McCabe"},
                fixed.getHeader());
        
        assertArrayEquals(
                new Object[] {"some/file.c", 14, "func1", 13.2, -34.2, null},
                fixed.getContent());
    }
    
    @Test
    public void testTryFixToFormat_WrongOrder() {
        MultiMetricResult mmr = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {13.2, -34.2}
        );
        String[] expecteMetrics = {"Fan Out", "Fan In"};
        
        MultiMetricResult fixed = AbstractKernelHavenRunner.tryFixToFormat(expecteMetrics, mmr);
        
        assertArrayEquals(
                new String[] {"Source File", "Line No.", "Element", "Fan Out", "Fan In"},
                fixed.getHeader());
        
        assertArrayEquals(
                new Object[] {"some/file.c", 14, "func1", -34.2, 13.2},
                fixed.getContent());
    }
    
    @Test
    public void testTryFixToFormat_WrongOrderAndMissing() {
        MultiMetricResult mmr = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {13.2, -34.2}
        );
        String[] expecteMetrics = {"Fan Out", "McCabe", "Fan In"};
        
        MultiMetricResult fixed = AbstractKernelHavenRunner.tryFixToFormat(expecteMetrics, mmr);
        
        assertArrayEquals(
                new String[] {"Source File", "Line No.", "Element", "Fan Out", "McCabe", "Fan In"},
                fixed.getHeader());
        
        assertArrayEquals(
                new Object[] {"some/file.c", 14, "func1", -34.2, null, 13.2},
                fixed.getContent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTryFixToFormat_DoubleActualName() {
        MultiMetricResult mmr = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan In"},
                new Double[] {13.2, -34.2}
        );
        String[] expecteMetrics = {"Fan Out", "Fan In"};
        
        AbstractKernelHavenRunner.tryFixToFormat(expecteMetrics, mmr);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTryFixToFormat_DoubleExpectedName() {
        MultiMetricResult mmr = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan Out"},
                new Double[] {13.2, -34.2}
        );
        String[] expecteMetrics = {"Fan Out", "Fan Out"};
        
        AbstractKernelHavenRunner.tryFixToFormat(expecteMetrics, mmr);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTryFixToFormat_ActualContainsMore() {
        MultiMetricResult mmr = new MultiMetricResult(new MeasuredItem("some/file.c", 14, "func1"),
                new String[] {"Fan In", "Fan Out", "McCabe"},
                new Double[] {13.2, -34.2, 2.0}
        );
        String[] expecteMetrics = {"Fan In", "Fan Out"};
        
        AbstractKernelHavenRunner.tryFixToFormat(expecteMetrics, mmr);
    }

    
}
