@rem Schema to instance tool
@rem

@echo off

setlocal
if "%XMLBEANS_LIB%" EQU "" call %~dp0_setlib

set cp=
set cp=%cp%;%XMLBEANS_LIB%\xbean.jar
set cp=%cp%;%XMLBEANS_LIB%\jsr173_api.jar

java -classpath %cp% org.apache.xmlbeans.impl.xsd2inst.SchemaInstanceGenerator %*

:done
