#!/bin/sh

tmpfile=$(mktemp)


echoHeader () {
cat <<EOF
/**
 *
 * Software written by Remi Emonet.
 *
 */
EOF
}

for javafile in "$@" ; do
(\
 echoHeader ;\
 awk 'BEGIN {p="false";} /^package/{p="true";} //{if(p=="true")print;}' "${javafile}"\
) > ${tmpfile}

(diff "${tmpfile}" "${javafile}"  > /dev/null ) || (echo "modifying ${javafile}" && cp "${tmpfile}" "${javafile}")
done
