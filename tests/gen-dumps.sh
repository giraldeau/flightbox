#!/bin/sh
out=textdump
mkdir -p $out
for i in $(ls -1d traces/*); do
    echo "dumping $i"
    lttv -m textDump -t $i > $out/$(basename $i).dump
done


