@rem Schema compiler
@rem
@rem Builds XBean types from xsd files.

@echo off

set cp=
set cp=%cp%;%XMLBEANDIR%\xbean.jar

java -classpath %cp% xmlbeans.apache.org.tool.SchemaCopy %*

:done
