@echo off
setlocal DisableDelayedExpansion
cd /d "%~dp0"
if errorlevel 1 goto failed
set "SIDEKICK_JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "SIDEKICK_JAVA=%JAVA_HOME%\bin\java.exe"
"%SIDEKICK_JAVA%" -version
if errorlevel 1 goto nojava
if not exist "managers-sidekick.jar" goto missingjar
if defined SIDEKICK_FONT goto customfont
"%SIDEKICK_JAVA%" -jar "managers-sidekick.jar"
if errorlevel 1 goto failed
exit /b 0
:customfont
"%SIDEKICK_JAVA%" "-Dsidekick.font=%SIDEKICK_FONT%" -jar "managers-sidekick.jar"
if errorlevel 1 goto failed
exit /b 0
:nojava
echo Install Java 21 or newer and reopen this launcher. See docs\DISTRIBUTION.md.
goto failed
:missingjar
echo Missing managers-sidekick.jar. Extract the entire ZIP first.
:failed
echo Startup failed. See the message above and docs\DISTRIBUTION.md.
pause
exit /b 1
