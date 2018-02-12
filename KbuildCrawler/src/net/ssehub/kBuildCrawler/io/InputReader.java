package net.ssehub.kBuildCrawler.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Passes the output (either std <b>or</b> err) of a {@link Process} to a String.
 * The other one will be passed to the console.
 * @author El-Sharkawy
 *
 */
public class InputReader extends Thread implements IProcessOutputHandler {

    private StringBuffer sb;
    private InputStream is;
    private boolean readStd;
    
    /**
     * Constructor to gobble in <tt>std</tt> output.
     */
    public InputReader() {
        this(true);
    }
    
    /**
     * Constructor to specify whether std <b>or</b> err should be passed to a String.
     * @param readStd <tt>true</tt> the output of <tt>std</tt> will be returned in {@link #getOutput()},
     *     <tt>false</tt> <tt>err</tt> will be returned.
     */
    public InputReader(boolean readStd) {
        this.readStd = readStd;
    }
    
    @Override
    public void gobble(Process process) {
        sb = new StringBuffer();
        if (readStd) {
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err, true, "Error Stream");
            errorGobbler.start();
            
            is = process.getInputStream();
            this.start();            
        } else {
            StreamGobbler inGobbler = new StreamGobbler(process.getInputStream(), System.out, true, "Std. Stream");
            inGobbler.start();
            
            is = process.getErrorStream();
            this.start();          
        }
    }

    @Override
    public void run() {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    /**
     * Returns the output of the {@link Process}.
     * @return std <b>or</b> err, depending on the settings passed to the constructor.
     */
    public String getOutput() {
        return sb.toString();
    }
}
