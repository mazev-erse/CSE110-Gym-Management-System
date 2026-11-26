@echo off
cd /d "%~dp0"
if not exist out mkdir out
echo Compiling FitPulse Pro Gym Management System (Java 11 target)...
javac --release 11 -d out src\gym\*.java src\gym\model\*.java src\gym\exception\*.java src\gym\repository\*.java src\gym\service\*.java src\gym\ui\*.java
if %errorlevel% neq 0 (
    echo Retrying with default javac target...
    javac -d out src\gym\*.java src\gym\model\*.java src\gym\exception\*.java src\gym\repository\*.java src\gym\service\*.java src\gym\ui\*.java
)
if %errorlevel% neq 0 (
    pause
    exit /b %errorlevel%
)
echo Launching FitPulse Pro Swing GUI...
java -cp out gym.Main
