package net.ssehub.kBuildCrawler.git;

public class ConfigProvider {

    private String url;
    
    ConfigProvider(String url) {
        this.url = url;
    }
    
    @Override
    public String toString() {
        return "Config: " + url;
    }
}
