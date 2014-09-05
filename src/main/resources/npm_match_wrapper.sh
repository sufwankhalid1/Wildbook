#!/bin/sh
export LD_LIBRARY_PATH=/usr/local/lib/opencv2.4.7

imgpath=$1
encdir=$2

/usr/bin/npm_match -sscale 1.1 15.16 "$encdir" "$imgpath" 0 0 2 0 -o "$imgpath.txt" -c "$imgpath.csv"

