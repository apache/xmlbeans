@rem Instance to Schema tool
@rem
@rem Builds xsd files from xml instance files.

@echo off

setlocal
if "%XMLBEANS_HOME%" EQU "" (set XMLBEANS_HOME=%~dp0..)

set cp=
set cp=%cp%;%XMLBEANS_HOME%\build\ar\xbean.jar
set cp=%cp%;%XMLBEANS_HOME%\build\lib\jsr173_api.jar
set cp=%cp%;%XMLBEANS_HOME%\build\lib\resolver.jar

java -classpath %cp% org.apache.xmlbeans.impl.inst2xsd.Inst2Xsd %*

:done
