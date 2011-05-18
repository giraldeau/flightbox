#!/bin/sh
out=textdump
mkdir -p $out

max=$(cat /proc/cpuinfo | grep processor| wc -l)

ls -1d traces/* | xargs -n 1 --max-procs=$max -I trace ./dump.sh trace
