#!/bin/bash

APPNAME=aplikasi-akademik

if [[ $# -eq 0 ]] ; then
    echo 'Cara pakai : deploy.sh <namafile.jar>'
    exit 1
fi

service $APPNAME stop
rm /var/lib/$APPNAME/$APPNAME.jar
ln -s /var/lib/$APPNAME/$1 /var/lib/$APPNAME/$APPNAME.jar
service $APPNAME start
