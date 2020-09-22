# snimon

usage

jdk1.7.0_80/bin/java \
 -DMONITOR_OUT_FILE=./outFile \
 -DMONITOR_LOG_FILE=./logFile \
 -DMONITOR_JPS_CMD="../../snifit/jdk1.7.0_80/bin/jps -v " \
 -DMONITOR_JMAP_CMD="../../snifit/jdk1.7.0_80/bin/jmap -histo:live" \
 -DMONITOR_PROC_SIGNATURE=RemoteMavenServer36 \
 -DMONITOR_INTERVAL_MINUTES=1 \
 -cp target/classes/ monitor.Monitor
