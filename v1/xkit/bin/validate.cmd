@rem Instance Validator
@rem
@rem Validates an instance against a schema.

@echo off

set cp=
set cp=%cp%;%XMLBEANDIR%\xbean.jar

java -classpath %cp% com.bea.xbean.tool.InstanceValidator %*

:done
