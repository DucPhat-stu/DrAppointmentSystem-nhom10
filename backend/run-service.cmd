@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-service.ps1" %*
exit /b %ERRORLEVEL%
