#!/bin/sh
breakchars="(){}[],^%$#@\"\";:''|\\"

classpath=''
for i in lib/*.jar; do
    export classpath=$classpath:$i
done
export templates_path=/Users/rdsr/work/src-code/tipsy-shell/compact_defs/templates

exec rlwrap --remember -c -b $breakchars -f .completions \
    java -cp $classpath clojure.main -e "(require 'tipsy-shell.core) (in-ns 'tipsy-shell.core) (clojure.core/use 'clojure.core)" --repl
