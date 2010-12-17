#!/bin/sh 

export LD_LIBRARY_PATH=/home/francis/workspace/lttv/ltt/.libs
java -cp "./lib/*:./bin:/usr/share/java/swt.jar" org.lttng.flightbox.MainUI $1
