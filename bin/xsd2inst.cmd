@rem Schema to instance tool
@rem

@echo off

setlocal
if "%XMLBEANS_HOME%" EQU "" (set XMLBEANS_HOME=%~dp0..)

set cp=
set cp=%cp%;%XMLBEANS_HOME%\build\ar\xbean.jar
set cp=%cp%;%XMLBEANS_HOME%\build\lib\jsr173_api.jar

java -classpath %cp% org.apache.xmlbeans.impl.xsd2inst.SchemaInstanceGenerator %*

:done
