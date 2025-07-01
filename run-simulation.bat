@echo off
echo Starting Client Simulation Launcher...
echo.
echo Make sure the Central Server is running first!
echo This will launch multiple simulated clients to test the server.
echo.
java -cp target/classes com.project.SimulationLauncher
pause 