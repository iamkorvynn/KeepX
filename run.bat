@echo off
REM KeepX — Secure Password Manager Launch Script
REM Requires: JDK 17+ on PATH or JAVA_HOME set

setlocal

REM Try JAVA_HOME first
if defined JAVA_HOME (
    set JAVA="%JAVA_HOME%\bin\java.exe"
) else (
    REM Try PATH
    where java >nul 2>&1
    if %errorlevel% equ 0 (
        set JAVA=java
    ) else (
        REM Fallback to Eclipse Adoptium common location
        set JAVA="C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot\bin\java.exe"
    )
)

REM Get the directory this script is in
set SCRIPT_DIR=%~dp0

%JAVA% -jar "%SCRIPT_DIR%target\KeepX.jar"
