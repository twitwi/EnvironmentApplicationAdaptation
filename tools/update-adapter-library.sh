#!/bin/sh

url=http://eaa.heeere.com/library.txt

if test $# -gt 0
then
    url=$1
    shift
fi

wget "$url" -O - | while read i ; do
    u=${url%/*}/$i
    echo "Getting adapter: $u"
    wget "$u"
done

