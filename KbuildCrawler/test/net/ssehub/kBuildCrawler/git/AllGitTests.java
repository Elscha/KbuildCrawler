package net.ssehub.kBuildCrawler.git;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kBuildCrawler.git.diff.AllGitDiffTests;

@RunWith(Suite.class)
@SuiteClasses({
    AllGitDiffTests.class,
    GitUtilsTest.class })
public class AllGitTests {

}
