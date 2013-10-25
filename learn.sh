#!/bin/bash

JAVAHOME="/usr/bin/"	#"/usr/local2/jdk1.7.0_25/jre/bin/"
CHARAPARSERHOME="/home/thomas/Desktop/new/" #"/usr/local2/CharaParser-1.0/"
LOGSHOME="/home/thomas/Desktop/logs/" #"/iplant/home/shared/charaparser_de_logs/"

#export PATH=/usr/local2/CharaParser-1.0/wordNetDB/bin:$PATH
#export WNHOME=/usr/local2/CharaParser-1.0/wordNetDB
#export WNSEARCHDIR=/usr/local2/CharaParser-1.0/wordNetDB/dict

#username=$(basename $(dirname $(dirname $(pwd)))) #basename/dirname to username folder
username="thomas"
timestamp=$(($(date +%s%N)/1000000))
internalId=$username$timestamp

#REPLACE USER PROVIDED ID WITH INTERNAL ID
parametersCopy=("$@")
while getopts ":i:c:z:w:f:g:j:k:b:e:r:l:a:n:p:d:u:s:t:" opt; do
     case $opt in
	z)
		userProvidedId=$OPTARG
		parametersCopy[$OPTIND-2]=$internalId
       		;;
      esac
done

reviewFile="workspace/$internalId/nextStep.txt"

#EXECUTE CHARAPARSER
if [ -n "$userProvidedId" ]; then
	#echo "$JAVAHOME/java -jar $CHARAPARSERHOME/learn/learn.jar ${parametersCopy[@]}"
	$JAVAHOME/java -jar $CHARAPARSERHOME/learn/learn.jar "${parametersCopy[@]}"
	#EXECUTED SUCCESSFULL?	
	if [ -f "$reviewFile" ]; then
		echo $internalId > $userProvidedId.learn
	else 
		echo "CharaParser execution failed"	
	fi
else 
	echo "Missing z parameter"
fi

#MOVE LOG FILES
for file in workspace/debug.log*; do
	filename=$(basename "$file")
	cp -v "$file" "$LOGSHOME/$username.$userProvidedId.$timestamp.learn.$filename"; 
done
