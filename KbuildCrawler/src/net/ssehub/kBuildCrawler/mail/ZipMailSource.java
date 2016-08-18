package net.ssehub.kBuildCrawler.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Uses one already downloaded GZ archive as mail source to minimize traffic.
 * @author El-Sharkawy
 *
 */
public class ZipMailSource implements IMailSource {
    
    private static final String FROM_START = "From: ";
    private static final String DATE_START = "Date: ";
    private static final String SUBJECT_START = "Subject: ";
    private static final String MESSAGE_ID_START = "Message-ID: ";
    
    private File zipArchive;
    
    /**
     * Single constructor of this class.
     * @param zipArchive The archive to use as source for extracting and parsing mails. 
     */
    public ZipMailSource(File zipArchive) {
        this.zipArchive = zipArchive;
    }

    @Override
    public List<Mail> loadMails() throws FileNotFoundException, IOException {
        List<String> content = new ArrayList<>();
        GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(zipArchive));
        InputStreamReader reader = new InputStreamReader(gzis);
        try (BufferedReader in = new BufferedReader(reader)) {
            String readed;
            while ((readed = in.readLine()) != null) {
                content.add(readed);
            }
        } catch (IOException e) {
            throw e;
        }
        
        
        return split(content);
    }

    /**
     * Splits the text into separate mails.
     * @param allLines The content of the GZ archive.
     * @return Parsed mails.
     */
    private List<Mail> split(List<String> allLines) {
        List<Mail> mails = new ArrayList<Mail>();
        int startIndex = 0;
        int endIndex = 0;
        // eMails from 0 to (n-1)
        for (int i = 1, end = allLines.size(); i < end; i++) {
            String line = allLines.get(i);
            if (line.startsWith(FROM_START)) {
                endIndex = i - 2;
                if (endIndex > startIndex) {
                    mails.add(createMail(allLines, startIndex, endIndex));
                }
                startIndex = i - 1;
            }
        }
        
        // last mail
        mails.add(createMail(allLines, endIndex + 1, allLines.size() - 1));
        
        return mails;
    }
    
    /**
     * Parses one single mail.
     * @param allLines The content of the GZ archive.
     * @param startIndex First index of the currently parsed mail.
     * @param endIndex Last index of the currently parsed mail.
     * @return The passed content structured in a {@link Mail}.
     */
    private Mail createMail(List<String> allLines, int startIndex, int endIndex) {
        String from = null;
        String date = null;
        String subject = null;
        StringBuffer content = new StringBuffer();
        boolean contentStarted = false;
        
        for (int i = startIndex; i <= endIndex; i++) {
            String line = allLines.get(i);
            if(!contentStarted) {
                if (line.startsWith(FROM_START)) {
                    from = line.substring(FROM_START.length(), line.length());
                } else if (line.startsWith(DATE_START)) {
                    date = line.substring(DATE_START.length(), line.length());
                } else if (line.startsWith(SUBJECT_START)) {
                    subject = line.substring(SUBJECT_START.length(), line.length());
                } else if (line.startsWith(MESSAGE_ID_START)) {
                    contentStarted = true;
                }
            } else {
                content.append(line);
                content.append("\n");
            }
        }
        
        return new Mail(from, date, subject, content.toString());
    }
}
