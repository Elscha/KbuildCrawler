package net.ssehub.kBuildCrawler;
import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kBuildCrawler.git.AllGitTests;
import net.ssehub.kBuildCrawler.mail.AllMailTests;
import net.ssehub.kBuildCrawler.metrics.MultiMetricJoinerTest;

@RunWith(Suite.class)
@SuiteClasses({
    AllMailTests.class,
    AllGitTests.class,
    MultiMetricJoinerTest.class,
    
    })
public class AllTests {
    public static final File TESTDATA = new File("testdata");
}
