package net.ssehub.kBuildCrawler.mail;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads and parses mails from given sources.
 * @author El-Sharkawy
 *
 */
public class MailParser {
    
    /**
     * Loads mails from the given resources.
     * @param sources A list of mail sources, from where to load Mails. 
     * @return A list of parsed {@link Mail}s. Will be at least an empty list, but not <tt>null</tt>.
     * @throws Exception In case of any errors.
     */
    public List<Mail> loadMails(IMailSource... sources) throws Exception {
        List<Mail> mails = new ArrayList<Mail>();
        
        if (null != sources) {
            for (int i = 0; i < sources.length; i++) {
                List<Mail> mailsToAdd = sources[i].loadMails();
                if (null != mailsToAdd) {
                    mails.addAll(mailsToAdd);   
                }
            }
        }
        
        return mails;
    }

}
