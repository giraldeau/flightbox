#!/bin/sh

test -d "$1" || exit 1

echo "dump $1"
out=strace/
mkdir -p $out
exec lttv -m formattedDump -F "%c.%e %s.%n %p %m" -t $1 > $out/$(basename $1).dump
