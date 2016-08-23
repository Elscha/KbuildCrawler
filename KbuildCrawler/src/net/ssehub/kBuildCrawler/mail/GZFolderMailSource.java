package net.ssehub.kBuildCrawler.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses one already downloaded GZ archives as mail source to minimize traffic.<br/>
 * This mail source takes a <b>folder containing GZ archives</b> from
 * <a href="https://lists.01.org/pipermail/kbuild-all/">https://lists.01.org/pipermail/kbuild-all/</a>
 * as input.
 * @author El-Sharkawy
 *
 */
public class GZFolderMailSource implements IMailSource {

    private File folder;
    
    /**
     * Single constructor for this mail source.
     * @param folder A folder containing GZ archives, will not
     * search recursively in sub folders for GZ archives.
     */
    public GZFolderMailSource(File folder) {
        this.folder = folder;
    }
    
    @Override
    public List<Mail> loadMails() throws Exception {
        if (!folder.isDirectory() || !folder.exists()) {
            throw new RuntimeException(folder.getAbsolutePath()
                + " is not a folder.");
        }
        
        File[] files = folder.listFiles();
        if (null == files) {
            throw new RuntimeException(folder.getAbsolutePath()
                + " containes no files.");
        }
        
        List<Mail> mails = new ArrayList<>();
        boolean gzFileFound = false;
        for (File file : files) {
            if (file.getName().toLowerCase().endsWith("gz")) {
                IMailSource gzFileParser = new ZipMailSource(file);
                try {
                    List<Mail> gzMails = gzFileParser.loadMails();
                    mails.addAll(gzMails);
                    gzFileFound = true;
                } catch (Exception e) {
                    // TODO
                }
            }
        } 
        
        if (!gzFileFound) {
            throw new RuntimeException(folder.getAbsolutePath()
                + " containes no GZ archives.");
        }
        
        return mails;
    }

}
