@rem Instance Validator
@rem
@rem Validates an instance against a schema.

@echo off

set cp=
set cp=%cp%;%XMLBEANDIR%\xbean.jar

java -classpath %cp% xml.apache.org.tool.InstanceValidator %*

:done
