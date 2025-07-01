@echo off
echo Compiling Java Distributed Inventory System...
echo.

if not exist target mkdir target
if not exist target\classes mkdir target\classes

echo Compiling source files...
javac -d target/classes -cp src/main/java src/main/java/com/project/*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Compilation successful!
    echo.
    echo You can now run:
    echo   - run-server.bat       (start the central server)
    echo   - run-client.bat       (start a branch client)
    echo   - run-client.bat "Branch-2"  (start second branch)
    echo   - run-simulation.bat   (start multiple simulated clients)
) else (
    echo.
    echo ✗ Compilation failed!
    echo Please check the error messages above.
)

echo.
pause 