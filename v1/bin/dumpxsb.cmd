@rem XSB file dumper
@rem
@rem Prints the contents of an xsb file in human-readable form
@echo off

setlocal
if "%XMLBEANS_HOME%" EQU "" (set XMLBEANS_HOME=%~dp0..)

set cp=

if EXIST %XMLBEANS_HOME%\build\ar\xbean.jar. (
    set cp=%cp%;%XMLBEANS_HOME%\build\ar\xbean.jar.
) else if EXIST %XMLBEANS_HOME%\lib\xbean.jar. (
    set cp=%cp%;%XMLBEANS_HOME%\lib\xbean.jar.
)

java -classpath %cp% org.apache.xmlbeans.impl.tool.XsbDumper %*

:done
