@echo off

@rem Invokes type hierarchy printer

setlocal
call _setlib

set cp=
set cp=%cp%;%XMLBEANS_LIB%\xbean.jar

java -classpath %cp% org.apache.xmlbeans.impl.tool.TypeHierarchyPrinter %*
