@echo off
echo Starting Central Warehouse Server...
echo.
echo Make sure no other application is using port 5000
echo Press Ctrl+C to stop the server
echo.
java -cp target/classes com.project.CentralServer
pause 