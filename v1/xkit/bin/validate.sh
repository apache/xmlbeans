#!/bin/sh
#
# Instance Validator
#
# Validates an instance against a schema.

cp=
cp=$cp:$XMLBEANDIR/xbean.jar

java -classpath $cp com.bea.xbean.tool.InstanceValidator $*