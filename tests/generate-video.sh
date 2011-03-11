#!/bin/sh

mpg () {
    BASE=$1
    NAME=$2
    echo ffmpeg -r 24 -i $1/$2/%05d.bmp $2.mpg
}

DIR=interval-render

mpg $DIR zoom 
mpg $DIR translate 
