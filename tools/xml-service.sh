#!/bin/sh

#omi=/home/prometheus/emonet/projects/omiscid-library
#test -d $omi || omi=/home/twilight/projects/omiscid-library
#omi=$omi/../jOMiSCID-Maven/target/OMiSCID-1.6.0.jar:$omi
export OMISCID_DNSSD_FACTORY=proxy
export OMISCID_DNSSD_FACTORY_VERBOSE_MODE=TRUE
moreopt=-Djava.library.path=/usr/lib/jni

if test $# -gt 0
then
    toKill=
    while test $# -gt 0
    do
        x=$1
        shift
        java -classpath projects/AdapterTools/target/*:lib/* \
            $moreopt \
            com.heeere.eaa.adaptertools.XMLServiceLauncher "$x" &
        toKill="$toKill $!"
    done
    trap "echo && echo killing $toKill && kill $toKill && echo killed" 2
    echo "waiting to be killed (in return, will kill$toKill)"
    cat > /dev/null
else
    echo "usage: $0 my-service.xml ... (any number of .xml service descriptors)"
fi


