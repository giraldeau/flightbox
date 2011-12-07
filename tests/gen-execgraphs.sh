#!/bin/sh

echo "starting..."

ABSPATH=$(readlink -f $0)
BASEDIR=$(dirname $ABSPATH)

export TRACE_DIR=${TRACE_DIR:-$BASEDIR/traces}
export TRACE_EXECGRAPH=${TRACE_SCRIPTS:-$BASEDIR/execgraph}

CORES=$(cat /proc/cpuinfo | grep processor | wc -l)

for TRACE in $(find $TRACE_DIR -mindepth 1 -type d); do
	NAME=$(basename $TRACE)
	OUTDIR=$TRACE_EXECGRAPH/$NAME
	#rm -rf $OUTDIR
	mkdir -p $OUTDIR
	echo processing $NAME
	java -cp "../bin/:../lib/*:/usr/share/java/*" org.lttng.flightbox.MainDependency -m cp -t $TRACE -d $OUTDIR > $OUTDIR/flightbox.log
	find $OUTDIR -name '*.dot' | xargs -P $CORES -n 1 sh -c 'dot -Tpng -o $1.png $1' inline
done

