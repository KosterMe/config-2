@echo off
chcp 65001 > nul
setlocal
rem Этап 1: вывод параметров из конфигурационных файлов.
cd /d "%~dp0.."

echo === 1. Default config.xml ===
call run.bat
echo exit code: %errorlevel%

echo === 2. Only required parameters ===
call run.bat examples\config\minimal.xml
echo exit code: %errorlevel%

echo === 3. Test repository mode ===
call run.bat examples\config\test_simple.xml
echo exit code: %errorlevel%
