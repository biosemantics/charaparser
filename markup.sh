#!/bin/bash
java -jar markup.jar "$@"

while getopts ":c:n:p:d:u:s:z:i:t:h:r:l:a:" opt; do
     case $opt in
         z)
             ID=$OPTARG
             ;;
      esac
done

#mkdir -p -v /iplant/home/shared/charaparser_de_logs/$ID/markup
#for file in workspace/debug.log*; do cp -v "$file" "/iplant/home/shared/charaparser_de_logs/$ID/markup/${file#workspace/}"; done
for file in workspace/debug.log*; do cp -v "$file" "/iplant/home/shared/charaparser_de_logs/$ID.markup.${file#workspace/}"; done
