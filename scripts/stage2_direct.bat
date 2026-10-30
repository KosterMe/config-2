@echo off
chcp 65001 > nul
setlocal
rem Этап 2: прямые зависимости пакетов из репозитория Alpine Linux.
cd /d "%~dp0.."

for %%f in (config.xml examples\config\minimal.xml examples\config\git.xml examples\config\python3.xml examples\config\nginx.xml) do (
    echo === %%f ===
    call run.bat %%f
    call echo exit code: %%errorlevel%%
)
