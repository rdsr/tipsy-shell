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

    cd compact_defs/templates
    export templates_path=`pwd`
    cd ../..
    echo "Updated env. var. templates_path to $templates_path"
fi

exec rlwrap --remember -c -b $breakchars -f completions \
    java -cp $classpath clojure.main -e "(require 'tipsy-shell.core) (in-ns 'tipsy-shell.core) (clojure.core/use 'clojure.core)" --repl
