@rem Schema compiler
@rem
@rem Builds XBean types from xsd files.

@echo off

setlocal
if "%XMLBEANS_HOME%" EQU "" (set XMLBEANS_HOME=%~dp0..)

set cp=
set cp=%cp%;%XMLBEANS_HOME%\build\ar\xbean.jar;%XMLBEANS_HOME%\build\lib\jsr173_api.jar

java -classpath %cp% org.apache.xmlbeans.impl.tool.SchemaCompiler %*

:done
