@rem Factors overlapping schemas into schemas that use imports

@echo off

set cp=
set cp=%cp%;%XMLBEANDIR%\xbean.jar

java -classpath %cp% xml.apache.org.tool.FactorImports %*

:done
