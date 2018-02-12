package net.ssehub.kBuildCrawler.io;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Implements a stream Gobbler.
 * 
 * @author Holger Eichelberger
 * @author El-Sharkawy
 */
public class StreamGobbler extends Thread {

    private InputStream is;
    private OutputStream out;
    private boolean isLineStream;
    // Only for the debugger
    @SuppressWarnings("unused")
    private String name;
    
    /**
     * Creates a stream gobbler.
     * 
     * @param is the input stream to be gobbled and emitted
     * @param out the target output stream where to pass the input to
     * @param isLineStream specification whether the given input can be read line by line (<tt>true</tt>)
     *     or must be handled char by char (<tt>false</tt>). Default is: <tt>true</tt>.
     * @param name The name of the stream, is only needed for debugging purpose.
     */
    public StreamGobbler(InputStream is, OutputStream out, boolean isLineStream, String name) {
        this.is = is;
        this.out = out;
        this.isLineStream = isLineStream;
        this.name = name;
    }

    /**
     * Creates standard gobblers for the given process.
     * 
     * @param proc the process to gobble
     */
    public static void gobble(Process proc) {
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), System.err, true, "Error Stream");
        errorGobbler.start();
        StreamGobbler outGobbler = new StreamGobbler(proc.getInputStream(), System.out, true, "Std Stream");
        outGobbler.start();
    }

    @Override
    public void run() {
        if (isLineStream) {
            handleLineStream();
        } else {
            handleTokenStream();
        }
    }
    
    /**
     * Reads the specified stream line by line.
     */
    private void handleLineStream() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                line += "\n";
                out.write(line.getBytes());
            }
        } catch (EOFException eof) {
            // ok, terminate
        } catch (IOException ioe) {
            // TODO
        }        
    }
    
    /**
     * Reads the specified stream char by char.
     */
    private void handleTokenStream() {
        try {
            int charNo = -1;
            while ((charNo = is.read()) != -1) {
                out.write(String.valueOf(charNo).getBytes());
            }
        } catch (EOFException eof) {
            // ok, terminate
        } catch (IOException ioe) {
            // TODO
        }        
    }
}
