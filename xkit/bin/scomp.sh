#!/bin/sh

#Schema compiler
#Builds XBean types from xsd files.


cp=
cp=$cp:$XMLBEANDIR/xbean.jar

java -classpath $cp com.bea.xbean.tool.SchemaCompiler $*


