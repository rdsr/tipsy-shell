#!/bin/sh
breakchars="(){}[],^%$#@\"\";:''|\\"

classpath=''
for i in lib/*.jar; do
    export classpath=$classpath:$i
done

if [ x${templates_path} == 'x' ]; then
    echo "Please set the env variable 'templates_path to /path/to/compact_defs_templates'"
    exit -1
fi

export $templates_path
exec rlwrap --remember -c -b $breakchars -f .completions \
    java -cp $classpath clojure.main -e "(require 'tipsy-shell.core) (in-ns 'tipsy-shell.core) (clojure.core/use 'clojure.core)" --repl
