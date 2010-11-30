#!/bin/sh 
# Generate test traces

if [ $(id -u) -ne 0 ]; then
    echo "This program must be run as root"
    exit 1
fi

echo "ltt-armall"
/usr/bin/ltt-armall -q

RET=$?
if [ $RET -ne 0 ]; then
    echo "error with ltt-armall, abording"
    exit 1
fi

echo "starting..."

ABSPATH=$(readlink -f $0)
BASEDIR=$(dirname $ABSPATH)

export TRACE_DIR=${TRACE_DIR:-$BASEDIR/traces}
export TRACE_SCRIPTS=${TRACE_SCRIPTS:-$BASEDIR/scripts}

mkdir -p $TRACE_DIR

for SCRIPT in $(find $TRACE_SCRIPTS -type f); do
    NAME=$(basename $SCRIPT)
    echo "tracing" $NAME
    TRACE_PATH=$TRACE_DIR/$NAME
    rm -rf $TRACE_PATH
    lttctl -o channel.all.bufnum=8 -C -w $TRACE_PATH $NAME 
    $SCRIPT
    lttctl -D $NAME
done

