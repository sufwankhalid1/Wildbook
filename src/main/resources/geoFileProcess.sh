#!/bin/sh

file=$1

zcat $file | /usr/local/bin/kml_to_json.pl > $file.json


