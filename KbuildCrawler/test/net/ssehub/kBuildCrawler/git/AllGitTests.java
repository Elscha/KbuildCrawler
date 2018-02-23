package net.ssehub.kBuildCrawler.git;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.ssehub.kBuildCrawler.git.diff.DiffUtilsTests;
import net.ssehub.kBuildCrawler.git.mail_parsing.GitUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({
    DiffUtilsTests.class,
    GitUtilsTest.class })
public class AllGitTests {

}
