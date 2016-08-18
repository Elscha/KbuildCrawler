package net.ssehub.kBuildCrawler.mail;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ZipMailSourceTests.class,
    // Should come after ZipMailSourceTests
    MailUtilsTest.class})
public class AllMailTests {

}
