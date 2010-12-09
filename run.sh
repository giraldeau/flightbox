#!/bin/sh 

java -cp "./lib/*:./bin:/usr/share/java/swt.jar" org.lttng.flightbox.junit.TestCpuUsageView $1
