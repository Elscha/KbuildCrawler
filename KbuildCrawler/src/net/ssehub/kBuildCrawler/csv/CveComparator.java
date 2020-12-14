package net.ssehub.kBuildCrawler.csv;

import java.util.Comparator;

import net.ssehub.kBuildCrawler.git.FailureTrace;

public class CveComparator implements Comparator<FailureTrace> {

    @Override
    public int compare(FailureTrace o1, FailureTrace o2) {
        CVEReport report1 = (o1 instanceof CVEReport) ? (CVEReport) o1 : null;
        CVEReport report2 = (o2 instanceof CVEReport) ? (CVEReport) o2 : null;
        
        String[] cve1Segments = null;
        String[] cve2Segments = null;
        boolean bothValid = false;
        if (null != report1 && null != report2) {
            String cve1 = report1.getDate();
            String cve2 = report2.getDate();
            
            int pos = -1;
            if ((pos = cve1.indexOf(',')) != -1) {
                cve1 = cve1.substring(0, pos);
            }
            if ((pos = cve2.indexOf(',')) != -1) {
                cve2 = cve2.substring(0, pos);
            }
                        
            cve1Segments = cve1.split("-");
            cve2Segments = cve2.split("-");
            bothValid = cve1Segments.length == 3 && cve2Segments.length == 3;            
        }
        
        int result;
        if (bothValid) {
            // Compare Year
            Integer year1 = Integer.valueOf(cve1Segments[1]);
            Integer year2 = Integer.valueOf(cve2Segments[1]);
            result = Integer.compare(year1, year2);
            if (0 == result) {
                Integer number1 = Integer.valueOf(cve1Segments[2]);
                Integer number2 = Integer.valueOf(cve2Segments[2]);
                result = Integer.compare(number1, number2);
            }
        } else {
            result = o1.getDate().compareTo(o2.getDate());
        }

        return result;
    }

}
