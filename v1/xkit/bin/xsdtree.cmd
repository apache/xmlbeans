@echo off

set cp=
set cp=%cp%;%XMLBEANDIR%\xbean.jar

java -classpath %cp% com.bea.xbean.tool.TypeHierarchyPrinter %*
