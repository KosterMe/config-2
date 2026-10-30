@echo off
chcp 65001 > nul
setlocal
rem Этап 2: ошибки получения данных из репозитория.
cd /d "%~dp0.."

for %%f in (examples\repo_errors\*.xml) do (
    echo === %%f ===
    call run.bat %%f
    call echo exit code: %%errorlevel%%
)
