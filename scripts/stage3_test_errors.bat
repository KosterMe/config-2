@echo off
chcp 65001 > nul
setlocal
rem Этап 3: ошибки в файлах тестового репозитория.
cd /d "%~dp0.."

for %%f in (examples\test_errors\*.xml) do (
    echo === %%f ===
    call run.bat %%f
    call echo exit code: %%errorlevel%%
)
