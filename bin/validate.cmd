@rem Instance Validator
@rem
@rem Validates an instance against a schema.

@echo off

setlocal
if "%XMLBEANS_LIB%" EQU "" call %~dp0_setlib

set cp=
set cp=%cp%;%XMLBEANS_LIB%\xbean.jar
set cp=%cp%;%XMLBEANS_LIB%\jsr173_api.jar

java -classpath %cp% org.apache.xmlbeans.impl.tool.InstanceValidator %*

:done
