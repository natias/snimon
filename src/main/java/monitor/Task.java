package monitor;

import java.io.*;
import java.util.Date;

public abstract class Task implements Runnable {


    final long intervalMinutes;
    final String jpsExe;
    final String processIdenifier;
    final String probeExe;
    final String outputFile;
    final Long heapLineCount;

    public Task(long intervalMinutes, String jpsExe, String processIdenifier, String probeExe, String outputFile, Long heapLineCount) {
        this.intervalMinutes = intervalMinutes;
        this.jpsExe = jpsExe;
        this.processIdenifier = processIdenifier;
        this.probeExe = probeExe;
        this.outputFile = outputFile;
        this.heapLineCount = heapLineCount;
    }

    @Override
    public void run() {

        while (true) {
            createReport();
            try {
                Thread.sleep(intervalMinutes * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public void createReport() {

        try {
            System.out.println("about to execute " + jpsExe);
            Process p = Runtime.getRuntime().exec(jpsExe);
            InputStream errors = p.getErrorStream();
            BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));

            //errors

            String line;
            while ((line = output.readLine()) != null) {
                if (line.contains(processIdenifier)) {
                    //System.out.println(line);
                    //System.out.println(line.split("\\s+")[0]);

                    String targetPid = line.split("\\s+")[0];

                    histDump(targetPid);


                }


            }

            p.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void histDump(String targetPid) throws IOException {


        System.out.println(new Date()+" about to execute " + probeExe + " " + targetPid);
        Process p = Runtime.getRuntime().exec(probeExe + " " + targetPid);
        InputStream errors = p.getErrorStream();
        BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        long linecount = 0;


        PrintStream ps = new PrintStream(new FileOutputStream(outputFile, true), true);

        ps.println("**** start dump for pid " + targetPid + " @" + new Date());
        while ((line = output.readLine()) != null) {
            if (lineFilter(linecount , heapLineCount,line)) {
                ps.println(line);
            }
            linecount++;

        }
        ps.println("**** end dump for pid " + targetPid + " @" + new Date());
        ps.close();

        p.destroy();


    }


    abstract boolean lineFilter(long currentLineNumber,long maxlines,String line);
}
