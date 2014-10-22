#! /bin/bash

VAR=${1:-/aikuma_var}
CREDENTIALS=$VAR/credentials.properties
TRACKING_DB=$VAR/tracking
T=`date +%s`    # beginning of this script
INTERVAL=86400  # 24 hours
LOG_PREFIX=$VAR/log-cpaf-

log() {
    echo `date +"[%Y-%m-%d %H:%M:%S]"` "$1" >>$LOG_FILE 2>&1
}

log_file_name() {
    echo ${LOG_PREFIX}$(date +%Y%m%d).txt
}

get_next_time() {
    echo $(( $T + ((`date +%s` - $T) / $INTERVAL + 1) * $INTERVAL ))
}

run_app() {
    java -jar aikuma-copy-archived-files.jar $CREDENTIALS $TRACKING_DB 
}

rm_old_logs() {
    find $VAR -name "${LOG_PREFIX}*" -ctime +7 -exec rm \{} \;
}

while true; do
    LOG_FILE=`log_file_name`

    log "STARTED"
    run_app >>$LOG_FILE 2>&1
    log "FINISHED with exit code $?"
    t=`get_next_time`
    log "Scheduled to run at `date -d @$t`"
    
    rm_old_logs >>$LOG_FILE 2>&1

    sleep $(( $t - `date +%s` ))
done


