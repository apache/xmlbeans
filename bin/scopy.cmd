@rem Schema compiler
@rem
@rem Builds XBean types from xsd files.

@echo off

setlocal
call _setlib

set cp=
set cp=%cp%;%XMLBEANS_LIB%\xbean.jar

java -classpath %cp% org.apache.xmlbeans.impl.tool.SchemaCopy %*

:done
