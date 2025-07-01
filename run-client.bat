@echo off
if "%1"=="" (
    set BRANCH_NAME=Branch-1
) else (
    set BRANCH_NAME=%1
)

echo Starting Branch Client: %BRANCH_NAME%
echo.
echo Make sure the Central Server is running first!
echo.
java -cp target/classes com.project.BranchClientApp %BRANCH_NAME%
pause 