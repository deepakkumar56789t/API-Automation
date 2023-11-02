@echo off
title Load test execution - LCM services

SETLOCAL EnableExtensions DisableDelayedExpansion
for /F %%a in ('echo prompt $E ^| cmd') do (
  set "ESC=%%a"
)

SETLOCAL EnableDelayedExpansion

echo.
echo !ESC![4;1mPlease paramaterize your JMeter(load test) execution!ESC![0m
echo.&echo.

REM echo !ESC![93mName of mirco service:!ESC![0m Subscription
set /p service=!ESC![93mEnter the name of mirco service:!ESC![0m 
set /p environment=!ESC![93mEnter the name of environment:!ESC![0m 
set /p threads=!ESC![93mEnter the thread/user count:!ESC![0m 
echo.

if not exist "Reports\" (mkdir Reports)

call jmeter -Jenvironment=%environment% -Jjmeter.reportgenerator.report_title=%environment% -Jthreads=%threads% -n -f -t ./%service%_API.jmx -l ./Reports/simple-data-writer.csv -e -o ./Reports/%service%_%environment%_%date:~-4%-%date:~3,2%-%date:~0,2%_%time:~0,2%-%time:~3,2%-%time:~6,2%
echo.

echo !ESC![92mLoad tested completed. kindly review the load test reports generated.!ESC![0m
echo.

pause