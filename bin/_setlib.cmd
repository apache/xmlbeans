@rem Common script to set the XMLBEANS_LIB variable
@rem to the directory containing xbean.jar
@echo off

if "%XMLBEANS_HOME%" EQU "" (set XMLBEANS_HOME=%~dp0..)

set XMLBEANS_LIB=

if exist %XMLBEANS_HOME%\build\lib\xbean.jar set XMLBEANS_LIB=%XMLBEANS_HOME%\build\lib
if exist %XMLBEANS_HOME%\lib\xbean.jar set XMLBEANS_LIB=%XMLBEANS_HOME%\lib

if "%XMLBEANS_LIB%" EQU "" echo "ERROR: Could not find xbean.jar"
