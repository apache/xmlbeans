#!/bin/sh

cp=
cp=$cp:$XMLBEANDIR/xbean.jar

java -classpath $cp com.bea.xbean.tool.FactorImports $*