@rem Schema compiler
@rem
@rem Builds jaxb types from xsd files.

@echo off

setlocal
if "%XMLBEANS_HOME%" EQU "" (set XMLBEANS_HOME=%~dp0..)

set cp=
set cp=%cp%;%XMLBEANS_HOME%\build\ar\xbean.jar
set cp=%cp%;%XMLBEANS_HOME%\external\lib\jaxb-1.0\jaxb-api.jar

java -classpath %cp% org.apache.xmlbeans.impl.tool.SchemaCompiler -jaxb %*

:done
