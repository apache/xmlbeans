@echo off

@rem Invokes type hierarchy printer

setlocal
if "%XMLBEANS_LIB%" EQU "" call %~dp0_setlib

set cp=
set cp=%cp%;%XMLBEANS_LIB%\xbean.jar

java -classpath %cp% org.apache.xmlbeans.impl.tool.TypeHierarchyPrinter %*
