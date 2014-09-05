#!/bin/sh
export LD_LIBRARY_PATH=/usr/local/lib/opencv2.4.7

encpath=$1

/usr/bin/npm_process -contr_thr 0.02 -sigma 1.2 "$encpath" 0 0 4 1 2

