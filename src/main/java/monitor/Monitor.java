package monitor;

import java.io.*;
import java.util.Date;


/**
 *
 * dump histogram of java process X identified by provided signature to file once every so and so minutes
 *
 */
public class Monitor {

    static final String outputFile=System.getProperty("MONITOR_OUT_FILE");
    static final String logFile=System.getProperty("MONITOR_LOG_FILE");
    static final String jpsExe=System.getProperty("MONITOR_JPS_CMD");
    static final String jmapExe=System.getProperty("MONITOR_JMAP_CMD");
    static final String processIdenifier=System.getProperty("MONITOR_PROC_SIGNATURE");
    static final Long heapLineCount=Long.parseLong(System.getProperty("MONITOR_HEAP_LINES","20"));
    static final Long intervalMinutes=Long.parseLong(System.getProperty("MONITOR_INTERVAL_MINUTES","5"));



    //Pattern whitespace = Pattern.compile("\\s\\s").;




    public static void main(String[] args) throws IOException {

        if(null!=logFile) {
            System.out.println("redirecting output to "+logFile);
            PrintStream ps=new PrintStream(  new FileOutputStream(logFile,true) ,true) ;

            System.setOut(ps);
            System.setErr(ps);
        }


        while (true) {
            createReport();
            try {
                Thread.sleep( intervalMinutes * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void createReport()
    {

        try {
            System.out.println("about to execute " + jpsExe);
            Process p=Runtime.getRuntime().exec(jpsExe);
            InputStream errors=p.getErrorStream();
            BufferedReader output=new BufferedReader(new InputStreamReader(p.getInputStream()));

            //errors

            String line;
            while ( (line=output.readLine()) !=null)
            {
                if(line.contains(processIdenifier))
                {
                    //System.out.println(line);
                    //System.out.println(line.split("\\s+")[0]);

                    String targetPid=line.split("\\s+")[0];

                    histDump(targetPid);


                }


            }

            p.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private static void histDump(String targetPid) throws IOException {


        System.out.println("about to execute " + jmapExe + " " + targetPid);
        Process p=Runtime.getRuntime().exec(jmapExe+" "+targetPid);
        InputStream errors=p.getErrorStream();
        BufferedReader output=new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        long linecount=0;


        PrintStream ps=new PrintStream(  new FileOutputStream(outputFile,true) ,true) ;

        ps.println("**** start dump for pid "+targetPid+" @"+new Date());
        while ( (line=output.readLine()) !=null)
        {
            if((linecount<=heapLineCount) || line.startsWith("Total"))
            {
                ps.println(line);
            }
            linecount++;

        }
        ps.println("**** end dump for pid "+targetPid +" @"+new Date());
        ps.close();

        p.destroy();


    }


}
