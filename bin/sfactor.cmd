@rem Schema Factoring tool
@rem
@rem Factores redundant definitions out of a set of schemas and uses imports instead.

@echo off

setlocal
if "%XMLBEANS_HOME%" EQU "" (set XMLBEANS_HOME=%~dp0..)

set cp=
set cp=%cp%;%XMLBEANS_HOME%\build\ar\xbean.jar

java -classpath %cp% org.apache.xmlbeans.impl.tool.FactorImports %*

:done
