package net.ssehub.kBuildCrawler.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputReader extends Thread implements IProcessOutputHandler {

    private StringBuffer sb;
    private InputStream is;
    private boolean readStd;
    
    public InputReader() {
        this(true);
    }
    
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
                if (null != line) {
                    sb.append(line);
                    sb.append("\n");
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public String getOutput() {
        return sb.toString();
    }
}
