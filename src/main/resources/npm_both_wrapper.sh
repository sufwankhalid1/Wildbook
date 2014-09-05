#!/bin/sh

imgpath=$1
encdir=$2


/usr/local/bin/npm_process_wrapper.sh "$imgpath"
/usr/local/bin/npm_match_wrapper.sh "$imgpath" "$encdir"


