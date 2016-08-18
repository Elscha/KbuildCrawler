package net.ssehub.kBuildCrawler.mail;

import java.util.List;

/**
 * A source from where to retrieve (parsed) {@link Mail}s.
 * @author El-Sharkawy
 *
 */
public interface IMailSource {
    
    /**
     * Loads mails from the specified source.
     * @return A list of parsed {@link Mail}s.
     * @throws Exception In case of any errors.
     */
    public List<Mail> loadMails() throws Exception;

}
