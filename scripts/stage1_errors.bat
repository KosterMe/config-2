@echo off
chcp 65001 > nul
setlocal
rem Этап 1: обработка ошибок для всех параметров.
cd /d "%~dp0.."

echo === Too many arguments ===
call run.bat a.xml b.xml
echo exit code: %errorlevel%

echo === Config file not found ===
call run.bat examples\config\missing.xml
echo exit code: %errorlevel%

for %%f in (examples\errors\*.xml) do (
    echo === %%f ===
    call run.bat %%f
    call echo exit code: %%errorlevel%%
)
