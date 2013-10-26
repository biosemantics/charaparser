#!/bin/bash

JAVAHOME="/usr/bin/"	#"/usr/local2/jdk1.7.0_25/jre/bin/"
CHARAPARSERHOME="/home/thomas/Desktop/new/" #"/usr/local2/CharaParser-1.0/"
LOGSHOME="/home/thomas/Desktop/losLogs/" #"/iplant/home/shared/charaparser_de_logs/"

#export PATH=/usr/local2/CharaParser-1.0/wordNetDB/bin:$PATH
#export WNHOME=/usr/local2/CharaParser-1.0/wordNetDB
#export WNSEARCHDIR=/usr/local2/CharaParser-1.0/wordNetDB/dict

#username=$(basename $(dirname $(dirname $(pwd)))) #basename/dirname to username folder
username="thomas"

#REPLACE USER PROVIDED ID WITH INTERNAL ID
parametersCopy=("$@")
while getopts ":i:c:z:w:f:g:j:k:b:e:r:l:a:n:p:d:u:s:t:" opt; do
     case $opt in
	z)
           	idFile=$OPTARG
		filename=$(basename "$idFile")	
		userProvidedId="${filename%.*}"
		internalId=$(<"$idFile")
		parametersCopy[$OPTIND-2]=$internalId
		;;
      esac
done

#EXECUTE CHARAPARSER MARKUP
if [ -n "$idFile" ]; then
	#echo "$JAVAHOME/java -jar $CHARAPARSERHOME/markup/markup.jar ${parametersCopy[@]}"
	$JAVAHOME/java -jar $CHARAPARSERHOME/markup/markup.jar "${parametersCopy[@]}"
else 
	echo "Missing z parameter"
fi

#MOVE LOG FILES
for file in workspace/debug.log*; do
	filename=$(basename "$file")
	cp -v "$file" "$LOGSHOME/$username.$userProvidedId.$timestamp.markup.$filename"
done
