package net.ssehub.kBuildCrawler.git.mail_parsing;

/**
 * Stores the location of a <tt>.config</tt> Kconfig file, which was used for a report.
 * @author El-Sharkawy
 *
 */
public class ConfigProvider {

    private String url;
    
    /**
     * Sole constructor for this class.
     * @param url The URL, from where the zipped <tt>.config</tt> Kconfig file can be retrieved.
     */
    ConfigProvider(String url) {
        this.url = url;
    }
    
    @Override
    public String toString() {
        return "Config: " + url;
    }
}
