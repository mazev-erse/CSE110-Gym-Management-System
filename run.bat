@echo off
cd /d "%~dp0"
if not exist out mkdir out
echo Compiling Gym Tracker...
javac --release 11 -d out src\gym\*.java
if %errorlevel% neq 0 (
    javac -d out src\gym\*.java
)
if %errorlevel% neq 0 (
    pause
    exit /b %errorlevel%
)
echo Launching Gym Tracker...
java -cp out gym.Main
