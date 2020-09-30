package monitor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * dump histogram of java process X identified by provided signature to file once every so and so minutes
 */
public class Monitor {

//    static final String outputFile=System.getProperty("MONITOR_OUT_FILE");
//
//    static final String jpsExe=System.getProperty("MONITOR_JPS_CMD");
//
//    static final String jmapExe=System.getProperty("MONITOR_JMAP_CMD");
//    static final Long jmapIntervalMinutes=Long.parseLong(System.getProperty("MONITOR_JMAP_INTERVAL_MINUTES","5"));
//    static final String logFile=System.getProperty("MONITOR_LOG_FILE");
//
//    static final String jstackExe=System.getProperty("MONITOR_JSTACK_CMD");
//    static final Long jstackIntervalMinutes=Long.parseLong(System.getProperty("MONITOR_JSTACK_INTERVAL_MINUTES","5"));
//
//
//    static final String processIdenifier=System.getProperty("MONITOR_PROC_SIGNATURE");
//    static final Long heapLineCount=Long.parseLong(System.getProperty("MONITOR_HEAP_LINES","20"));


    //Pattern whitespace = Pattern.compile("\\s\\s").;


    static final String config_json = System.getProperty("CONFIG_JSON", "config.json");

    public static void main(String[] args) throws IOException {

        JSONObject config = new JSONObject(new String(Files.readAllBytes(Paths.get(config_json)), StandardCharsets.UTF_8));


        String logFile = config.getString("output");


        if (null != logFile) {
            System.out.println("redirecting output to " + logFile);
            PrintStream ps = new PrintStream(new FileOutputStream(logFile, true), true);

            System.setOut(ps);
            System.setErr(ps);
        }


        String jpsExe = config.getString("jps_command");
        String processIdenifier = config.getString("proc_signature");


        JSONArray probes = config.getJSONArray("probes");



        /*


         "name": "jstack",
      "cmd": "../../snifit/jdk1.7.0_80/bin/jstack ",
      "output": "jstack.out",
      "interval_minutes": 10,
      "max_lines": 0
         */

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);


        for (int i = 0; i < probes.length(); i++) {

            JSONObject probe = probes.getJSONObject(i);

            String name=probe.getString("name");
            String cmd=probe.getString("cmd");
            String output=probe.getString("output");
            Long interval=probe.getLong("interval_minutes");
            Long maxLines=probe.getLong("max_lines");
            final String footer=probe.getString("footer");

            Task task = new Task(interval, jpsExe, processIdenifier,cmd,output,maxLines ) {
                @Override
                boolean lineFilter(long currentLine, long maxlinesX, String line) {
                    return ( (maxlinesX==0) || currentLine < maxlinesX) || (!footer.isEmpty() && line.startsWith(footer));
                }
            };

            executor.execute(task);

        } ;




    }


}
