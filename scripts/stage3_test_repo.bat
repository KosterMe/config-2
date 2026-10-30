@echo off
chcp 65001 > nul
setlocal
rem Этап 3: граф зависимостей для разных тестовых репозиториев.
cd /d "%~dp0.."

call :show test_chain chain
call :show test_cycle cycle
call :show test_missing missing_deps
call :show test_multi_cycle multi_cycle
call :show test_self_loop self_loop
call :show test_simple simple
call :show test_subgraph multi_cycle
exit /b 0

:show
echo === examples\config\%1.xml: repository examples\repos\%2.txt ===
type examples\repos\%2.txt
echo ---
call run.bat examples\config\%1.xml
echo exit code: %errorlevel%
exit /b 0
