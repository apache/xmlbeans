@rem Schema Factoring tool
@rem
@rem Factores redundant definitions out of a set of schemas and uses imports instead.

@echo off

setlocal
call _setlib

set cp=
set cp=%cp%;%XMLBEANS_LIB%\xbean.jar

java -classpath %cp% org.apache.xmlbeans.impl.tool.FactorImports %*

:done
