#!/bin/bash

databaseHost = ""
databasePort=""
databaseName=""
databaseUser=""
databasePassword=""
logDirectory="/iplant/home/shared/charaparser_de_logs/"
maxDays="90"

java -jar cleanup.jar $databaseHost $databasePort $databaseName $databaseUser $databasePassword $maxDays
find $logDirectory -type f -mtime +$maxDays -exec rm -rf {} +
