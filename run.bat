@echo off
REM Set environment variables
set "DB_USERNAME=root"
set "DB_PASSWORD=password"

REM Compile all Java files
echo Compiling Java files...
javac -cp ".;lib/*" -d bin src\main\java\com\school\**\*.java src\main\java\com\school\*.java

if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b %errorlevel%
)

REM Run the main class
echo Running application...
java -cp "bin;lib/*" com.school.Main
pause
