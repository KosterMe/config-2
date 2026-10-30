@echo off
rem Build and run the tests.
setlocal
set "ROOT=%~dp0"
cd /d "%ROOT%"
javac -encoding UTF-8 -d "%ROOT%out\classes" -sourcepath "%ROOT%src" "%ROOT%src\depviz\Main.java"
if errorlevel 1 exit /b 1
javac -encoding UTF-8 -d "%ROOT%out\test-classes" -cp "%ROOT%out\classes" ^
    -sourcepath "%ROOT%tests" "%ROOT%tests\depviz\TestRunner.java"
if errorlevel 1 exit /b 1
java -cp "%ROOT%out\classes;%ROOT%out\test-classes" depviz.TestRunner
