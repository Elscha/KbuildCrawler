package net.ssehub.kBuildCrawler.mail;

/**
 * A parsed eMail (data object).
 * @author El-Sharkawy
 *
 */
public class Mail {

    private String from;
    private String date;
    private String subject;
    private String content;
    
    /**
     * Single constructor for this class.
     * @param from From where the mail comes from (must not be <tt>null</tt>).
     * @param date When was the mail sent (must not be <tt>null</tt>).
     * @param subject The subject of the mail(must not be <tt>null</tt>).
     * @param content The content of the mail (must not be <tt>null</tt>).
     */
    Mail(String from, String date, String subject, String content) {
        this.from = from;
        this.date = date;
        this.subject = subject;
        this.content = content;
    }

    /**
     * Returns the sender.
     * @return E-Mail address (name).
     */
    public String getFrom() {
        return from;
    }

    /**
     * When was the mail sent.
     * @return A String in form of Day of Week, DD MMM YYYY HH:MM:SS Timezone, e.g. <br/>
     * <tt>Mon, 1 Aug 2016 18:33:49 +0800</tt>
     */
    public String getDate() {
        return date;
    }

    /**
     * The subject of the mail.
     * @return The subject.
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * The content of the mail.
     * @return The content.
     */
    public String getContent() {
        return content;
    }
    
    @Override
    public String toString() {
        return "From: " + from + " with subject: " + subject + " wrote:\n" + content;
    }
}
