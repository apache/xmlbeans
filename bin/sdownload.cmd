@rem Schema downloader
@rem
@rem Tool to download schemas.

@echo off

setlocal
call _setlib

set cp=
set cp=%cp%;%XMLBEANS_LIB%\xbean.jar
set cp=%cp%;%XMLBEANS_LIB%\jsr173_api.jar
set cp=%cp%;%XMLBEANS_LIB%\resolver.jar

java -classpath %cp% org.apache.xmlbeans.impl.tool.SchemaResourceManager %*

:done
