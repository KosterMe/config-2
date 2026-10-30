@echo off
rem Build and run the tool. All arguments are passed to it.
setlocal
set "ROOT=%~dp0"
javac -encoding UTF-8 -d "%ROOT%out\classes" -sourcepath "%ROOT%src" "%ROOT%src\depviz\Main.java"
if errorlevel 1 exit /b 1
java -cp "%ROOT%out\classes" depviz.Main %*
