@echo off
rem Wrapper script to delegate to the Kotlin Gradle wrapper

setlocal
set SCRIPT_DIR=%~dp0
call "%SCRIPT_DIR%src\kotlin\gradlew.bat" -p "%SCRIPT_DIR%src\kotlin" %*
