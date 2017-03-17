#!/bin/bash

java -jar /app.jar --verbose $@ &

mappings_watch_reload() {
    chsum1=""

    while [[ true ]]
    do
        chsum2=`find /mappings -type f -exec md5sum {} \;`
        if [[ $chsum1 != $chsum2 ]] ; then
            echo "/mappings updated, reseting wiremock"
            curl -X POST http://localhost:8080/__admin/reset
            chsum1=$chsum2
        fi
        sleep 2
    done
}

mappings_watch_reload