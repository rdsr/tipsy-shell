#!/bin/sh
#mvn dependency:copy-dependencies -DoutputDirectory=lib
#mvn clean install
#cp target/*.jar lib

breakchars="(){}[],^%$#@\"\";:''|\\"

classpath='.'
for i in lib/*.jar; do
    export classpath=$classpath:$i
done

if [ -z "$templates_path" ]; then
    echo "Please set the env variable 'templates_path to /path/to/compact_defs_templates'"
    exit -1
fi

exec rlwrap --remember -c -b $breakchars -f completions \
    java -cp $classpath clojure.main -e "(require 'tipsy-shell.core) (in-ns 'tipsy-shell.core) (clojure.core/use 'clojure.core)" --repl
